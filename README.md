# Mini
[![Release](https://jitpack.io/v/hyperdevs-team/mini-kotlin.svg)](https://jitpack.io/#hyperdevs-team/mini-kotlin)

## ⚠️ DOCS HAVE TO BE UPDATED FOR VERSION 3.x.y

Mini is a minimal Flux architecture written in Kotlin that also adds a mix of useful features to build UIs fast.

## Purpose
You should use this library if you aim to develop a reactive application with good performance (no reflection using code-gen).
Feature development using Mini is fast compared to traditional architectures (like CLEAN or MVP), low boilerplate and state based models make feature integration and bugfixing easy as well as removing several families of problems like concurrency or view consistency across screens.

## How to Use
### Dispatcher
The *Dispatcher* is the hub that manages all data flow in a Flux application. It is basically a holder of store callbacks: each store registers itself and provides a callback for an action.

One important thing is that the dispatching is always performed in the same thread to avoid possible side-effects.

We can dispatch actions in the following ways:

```kotlin
// Dispatch an action on the main thread synchronously
dispatcher.dispatch(action = LoginAction(username = "user", password = "123"))

// Dispatch an action on the given scope
dispatcher.dispatchOn(action = LoginAction(username = "user", password = "123"), scope = coroutineScope)
```

### Store
The *Stores* are holders for application state and state mutation logic. In order to do so they expose pure reducer functions that are later invoked by the dispatcher. A *Store* is a type of a *StateContainer*, which is exactly that: a container of states.

The state is a plain object (usually a `data class`) that holds all information needed to display the view. States should always be immutable. State classes should avoid using framework elements (View, Camera, Cursor...) in order to facilitate testing.

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
@Action
data class LoginAction(val username: String, val password: String)
```

When we receive the response from the server, we'll dispatch another action with the result:
```kotlin
@Action
data class LoginCompleteAction(val loginTask: Task, val user: User?)
```

Actions will usually be triggered from Views or Controllers.

### View changes
Each ``StateContainer`` exposes a Kotlin `Flow` that emits changes produced on the state, allowing the view to listen reactive those changes. Being able to update the UI according to the new `StateContainer` state.

```kotlin  
mainStore.flow()
         .onEach { state ->
              // Do whatever you want
         }.launchInLifecycleScope()
```  

### Tasks
A `Task` is a basic object to represent an ongoing process. They should be used in the state of our `StateContainer` (a `Store`, for example) to represent ongoing processes that must be represented in the UI.

You can also use `TypedTask` to save metadata related the current task. 

**IMPORTANT: Do not use TypedTask to hold values that must survive multiple task executions. Save them as a variable in the state instead.**


### Example
Given the example Stores and Actions explained before, the workflow would be:

- View dispatches `LoginAction`.
- Store changes his `LoginTask` status to running and call though his SessionController which will do all the async work to log in the given user.
- View shows an Spinner when `LoginTask` is in running state.
- The async call ends and `LoginCompleteAction` is dispatched on UI, sending a null `User` and an error state `Task` if the async work failed or a success `Task` and an `User`.
- The Store changes his state to the given values from `LoginCompleteAction`.
- The View redirect to the HomeActivity if the task was success or shows an error if not.

You can execute another sample in the `app` package. It contains two different samples executing two types of `StateContainer`s:
- `StoreSampleActivity` class uses a `Store` as a `StateContainer`.
- `ViewModelSampleActivity` class uses a `ViewModel` as a `StateContainer`.

## Kotlin Flow Utils
Mini includes some utility extensions over Kotlin `Flow` to make easier listen state changes over the `StateContainer`s.

- `select`: Will emit only not null values over the given `map` clause.
- `selectNotNull`: Like `select` but avoiding null values.
- `onEachChange`: Emits a value when the values goes from one value to another.
- `onEachDisable`: Emits when the value goes from true to false.
- `onEachEnable`: Emits when the value goes from false to true.

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
Mini includes a custom `LoggerMiddleware` to log any change in your `StateContainer` states produced from an `Action`. This will allow you to keep track of your actions, changes and side-effects more easily. 
To add the LoggerMiddleware to your application you just need to add a single instance of it to your `Dispatcher` after initialize it in your `Application` class or dependency injection code.
```kotlin
val loggerMiddleware = CustomLoggerMiddleware(stores().values)
dispatcher.addMiddleware(loggerMiddleware)
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
    def mini_version = "3.0.0"
    // Minimum working dependencies
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-android:$mini_version"
    kapt "com.github.hyperdevs-team.mini-kotlin:mini-processor:$mini_version"

    // Kodein helper libraries
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-kodein:$mini_version"
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-kodein-android:$mini_version"
    // Kodein helper library for view models scoped to the Navigation component's graph in Jetpack Compose
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-kodein-android-compose:$mini_version"

    // Testing helper libraries
    androidTestImplementation "com.github.hyperdevs-team.mini-kotlin:mini-testing:$mini_version"
}
```

### Recommended settings
#### JDK8 requirements
Ensure that your project has compatibility with Java 8:
```groovy
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```
#### Improve compilation speed
In order to speed up the compilation process, it is recommended to add the following settings in
your `gradle.properties`:
```groovy
## Improves kapt speed with parallel annotation processing tasks, may impact in memory usage
kapt.use.worker.api=true
## Enables Gradle build cache
org.gradle.caching=true
```

### \[Android] Setting up your App file

You'll need to add the following snippet to your `Application`'s `onCreate` method. If you don't have it, then create it and reference it in your `AndroidManifest.xml` file:

```kotlin
val stores = listOf<Store<*>>() // Here you'll set-up you store list, you can retrieve it using your preferred DI framework
val dispatcher = MiniGen.newDispatcher() // Create a new dispatcher

// Initialize Mini
storeSubscriptions = MiniGen.subscribe(dispatcher, stores)
stores.forEach { store ->
    store.initialize()
}

// Optional: add logging middleware to log action events
dispatcher.addMiddleware(LoggerMiddleware(stores)) { tag, msg ->
    Log.d(tag, msg)
}
```

### \[Android] Kodein Android utils
`mini-kodein-android` has some utils methods in order to inject an Android's `ViewModel` in a `DIAware` `Activity` or `Fragment`.
To use it, bind with kodein the Android's `ViewModelProvider.Factory` instance:
```kotlin
// Use any tag to differ between the injected `Context` or `Application` if you are binding also `Context` with Kodein
bind<Application>("appTag") with singleton { app }
bind<ViewModelProvider.Factory>() with singleton { DIViewModelFactory(di.direct) }
```
To inject a view model without parameters, bind it as follows in this example:
```kotlin
bindViewModel { MainViewModel(instance("appTag") }
```
And in your `DIAware` `Activity` or `Fragment`:
```kotlin
private val mainViewModel: MainViewModel by viewModel()
```

`mini-kodein-android-compose` has some utils methods in order to inject an Android's `ViewModel` in the scope of a Navigation component
graph. This is useful as in Jetpack Compose is common to have only one or few `Activities` and none `Fragment` so, in order to scope
the lifecycle of the `ViewModel` not for all the life of the `Activity`, we can scope it to any route existing in the `NavBackStackEntry`.
For use it, do the same as above, but instead of injecting the view model scoped to a route of the Navigation, the `NavHost` composable
must be inside an `DIAware Activity`, and then do as follows:
```kotlin
composable(route = "home") { navBackStackEntry ->
    val homeViewModel: HomeViewModel by navBackStackEntry.viewModel(contextDI())
    HomeScreen(homeViewModel, ...)
}
```

In case you want to pass an argument to a view model, you need to bind the factory of that kind of Android's ViewModel.
You can do this in both `mini-kodein-android`, and `mini-kodein-android-compose`.
For example given a view model that you want to pass a `String` param, it would be:
```kotlin
bindViewModelFactory<HomeViewModelWithParameter, ViewModelProvider.Factory> { param ->
    TypedViewModelFactory(HomeViewModelWithParameter::class, instance("appTag"), param as String)
}
```
And for retrieve it with the given param in its constructor:
```kotlin
val param = "Hello World!"
val homeViewModelWithParameter: HomeViewModelWithParameter by navBackStackEntry.viewModel(contextDI(), param)
```

### \[Android] Proguard/R8
Each of the libraries contain a sensible proguard file that your project can consume in order to run you app on Proguard or R8.
No additional steps have to be done in order to use them apart from enabling minify in your project.

## Acknowledgements
The work in this repository up to April 30th, 2021 was done by [bq](https://github.com/bq).
Thanks for all the work!!

## License
This project is licensed under the Apache Software License, Version 2.0.
```
   Copyright 2021 HyperDevs
   
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
