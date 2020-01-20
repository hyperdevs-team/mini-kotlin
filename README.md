# Mini
[![Release](https://jitpack.io/v/masmovil/mini-kotlin.svg)](https://jitpack.io/#masmovil/mini-kotlin)

Mini is a minimal Flux architecture written in Kotlin that also adds a mix of useful features to build UIs fast.

## Purpose
You should use this library if you aim to develop a reactive application with good performance (no reflection using code-gen).
Feature development using Mini is fast compared to traditional architectures (like CLEAN or MVP), low boilerplate and state based models make feature integration and bugfixing easy as well as removing several families of problems like concurrency or view consistency across screens.

## How to Use
### Dispatcher
The *Dispatcher* is the  hub that manages all data flow in a Flux application. It is basically a holder of store callbacks:  each store registers itself and provides a callback for an action.

One important thing is that the dispatching is always performed in the same thread to avoid possible side-effects.

We can dispatch actions in the following ways:

```kotlin
// Dispatch an action on the main thread synchronously
dispatcher.dispatch(LoginAction(username = "user", password = "123"))

// Post an event that will dispatch the action on the UI thread and return immediately.
dispatcher.dispatchAsync(LoginAction(username = "user", password = "123"))
```

### Store
The *Stores* are holders for application state and state mutation logic. In order to do so they expose pure reducer functions that are later invoked by the dispatcher.

The state is a plain object (usually a `data class`) that holds all information needed to display the view. States should always be inmutable. State classes should avoid using framework elements (View, Camera, Cursor...) in order to facilitate testing.

Stores subscribe to actions to change the application state after a dispatch. Mini generates the code that links dispatcher actions and stores using the `@Reducer` annotation over a **non-private function that receives an Action as parameter**.

```kotlin
data class SessionState(val loginTask: Task = taskIdle(), val loggedUser: User? = null)

class SessionStore @Inject constructor(val controller: SessionController) : Store<SessionState>() {
    @Reducer
    fun login(action: LoginAction): SessionState {
        controller.login(action.username, action.password)
        return state.copy(loginTask = taskRunning(), loggedUser = null)
    }

    @Reducer
    fun loginComplete(action: LoginCompleteAction): SessionState {
        return state.copy(loginTask = action.loginTask, loggedUser = action.user)
    }
}
```

### Actions
An *Action* is a simple class that usually represents a use case. It can also contain a payload that includes data to perform said action. When an action is triggered, it will be delivered via dispatcher to the stores that are going to do something with the action to change their state.

For example, we may want to log in to a service. We would create an action like this one:
```kotlin
data class LoginAction(val username: String, val password: String)
```

When we receive the response from the server, we'll dispatch another action with the result:
```kotlin
data class LoginCompleteAction(val loginTask: Task, val user: User?)
```

Actions will usually be triggered from Views or Controllers.

### View changes
Each ``Store`` exposes a custom `StoreCallback` though the method `observe` or a `Flowable` if you want to make use of RxJava. Both of them emits changes produced on their states, allowing the view to listen reactive the state changes. Being able to update the UI according to the new `Store` state.

```kotlin
  //Using RxJava  
  userStore
          .flowable()
          .map { it.name }
          .subscribe { updateUserName(it) }
          
  // Custom callback      
  userStore
          .observe { state -> updateUserName(state.name) }
```  

If you make use of the RxJava methods, you can make use of the `SubscriptionTracker` interface to keep track of the `Disposables` used on your activities and fragments.

### Tasks
A Task is a basic object to represent an ongoing process. They should be used in the state of our `Store` to represent ongoing processes that must be represented in the UI.

Given the example Stores and Actions explained before, the workflow will be:

- View dispatch `LoginAction`.
- Store changes his `LoginTask` status to running and call though his SessionController which will do all the async work to log in the given user.
- View shows an Spinner when `LoginTask` is in running state.
- The async call ends and `LoginCompleteAction` is dispatched on UI, sending a null `User` and an error state `Task` if the async work failed or a success `Task` and an `User`.
- The Store changes his state to the given values from `LoginCompleteAction`.
- The View redirect to the HomeActivity if the task was success or shows an error if not.

## Rx Utils
Mini includes some utility extensions over RxJava 2.0 to make easier listen state changes over the `Stores`.

- `mapNotNull`: Will emit only not null values over the given `map` clause.
- `select`: Like `mapNotNull` but avoiding repeated values.
- `onNextTerminalState`: Used to map a `Task` inside an state and listen the next terminal state(Success - Error). Executing a different closure depending of the result of the task.

## Navigation
To avoid loops over when working with navigation based on a process result. You will need to make use of `onNextTerminalState` after dispatch and `Action` that starts a process which result could navigate to a different screen.
For example:
```kotlin
  fun login(username: String, password: String) {
        dispatcher.dispatch(LoginAction(username, password))
        sessionStore.flowable()
                .onNextTerminalState(taskMapFn = { it.loginTask },
                        successFn = { navigateToLogin() },
                        failureFn = { showError(it) })
    }
```

If we continually listen the changes of a `Task` and we navigate to a specific screen when the `Task` becomes successful. The state will stay on SUCCESS and if we navigate back to the last screen we will be redirected again.

## Logging
Mini includes a custom `LoggerInterceptor` to log any change in your `Store` states produced from an `Action`. This will allow you to keep track of your actions, changes and side-effects more easily. 
To add the LoggerInterceptor to your application you just need to add a single instance of it to your `Dispatcher` after initialize it in your `Application` class or dependency injection code.
```kotlin
val loggerInterceptor = CustomLoggerInterceptor(stores().values)
dispatcher.addInterceptor(loggerInterceptor)
```

## Testing with Mini
Mini includes an extra library called mini-android-testing with a few methods and `Expresso TestRules` to simplify your UI tests over this architecture.

- `TestDispatcherRule` : This rule will intercept any action that arrives to the Dispatcher, avoiding any call to the Store and their controllers. If we include this rule we will need to change the states manually in our tests.
- `CleanStateRule` : It just reset the state of your stores before and after each test.

Example of test checking that an action is correctly dispatched:

```kotlin
@get:Rule
val testDispatcher = testDispatcherRule()

@Test
fun login_button_dispatch_login_action() {
    onView(withId(R.id.username_edit_text)).perform(typeText("someUsername"))
    onView(withId(R.id.password_edit_text)).perform(typeText("somePassword"))
    onView(withId(R.id.login_button)).perform(click())
    
    assertThat(testDispatcher.actions, contains(LoginAction(someUsername, somePassword)))
}
```

Example of test checking that a view correctly changes with an specify state:

```kotlin
@get:Rule
val cleanState = cleanStateRule()

@Test
fun login_redirects_to_home_with_success_task() {
     //Set login state to success
     onUiSync {
         val loggedUser = User(email = MockModels.anyEmail, uid = MockModels.anyId, username = MockModels.anyUsername, photoUrl = MockModels.anyPhoto)
         val state = SessionState().copy(loginRequestState = requestSuccess(), verified = false, loggedIn = true, loggedUser = loggedUser)
         sessionStore.setTestState(state)
     }
     //Redirect to Email verification activity
     intended(hasComponent(HomeActivity::class.java.name))
}
```

## Setting Up
### Import the library

Add the following dependencies to your main `build.gradle`:
```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Add the following dependencies to your app's `build.gradle`:

```groovy
dependencies {
    def mini_version = "1.0.9"
    // Minimum working dependencies
    implementation "com.github.masmovil.mini-kotlin:mini-android:$mini_version"
    kapt "com.github.masmovil.mini-kotlin:mini-processor:$mini_version"

    // RxJava 2 helper libraries
    implementation "com.github.masmovil.mini-kotlin:mini-rx2:$mini_version"
    implementation "com.github.masmovil.mini-kotlin:mini-rx2-android:$mini_version"

    // Kodein helper libraries
    implementation "com.github.masmovil.mini-kotlin:mini-kodein:$mini_version"
    implementation "com.github.masmovil.mini-kotlin:mini-kodein-android:$mini_version"

    // Testing helper libraries
    androidTestImplementation "com.github.masmovil.mini-kotlin:mini-testing:$mini_version"
}
```

### [Android] Setting up your App file

You'll need to add the following snippet to your `Application`'s `onCreate` method. If you don't have it, then create it and reference it in your `AndroidManifest.xml` file:

```kotlin
val stores = listOf<Store<*>>(...) // Here you'll set-up you store list, you can retrieve it using your preferred DI framework
val dispatcher = MiniGen.newDispatcher() // Create a new dispatcher

// Initialize Mini
storeSubscriptions = MiniGen.subscribe(dispatcher, stores)
stores.forEach { store ->
    store.initialize()
}

// Optional: add logging interceptor to log action events
dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
    Log.d(tag, msg)
}))
```

## License
```
   Copyright 2019 BQ

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
