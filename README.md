# Mini
[![Release](https://jitpack.io/v/hyperdevs-team/mini-kotlin.svg)](https://jitpack.io/#hyperdevs-team/mini-kotlin)

Mini is a minimal Flux architecture written in Kotlin that also adds a mix of useful features to build UIs fast.

## Purpose
You should use this library if you aim to develop a reactive application with good performance (no reflection using code-gen).
Feature development using Mini is fast compared to traditional architectures (like CLEAN or MVP), low boilerplate and state based models make feature integration and bugfixing easy as well as removing several families of problems like concurrency or view consistency across screens.

## Setting Up
### Import the library

First, add the following dependencies to your main `build.gradle` so you can import the library dependencies:

<details open><summary>Groovy</summary>

```groovy
buildscript {
    repositories { 
        maven { url "https://jitpack.io" }
    }
}
```

</details>

<details><summary>Kotlin</summary>

```kotlin
buildscript {
    repositories { 
        maven("https://jitpack.io")
    }
}
```

</details>

Then, add the following dependencies to your module's `build.gradle`:

<details open><summary>Groovy</summary>

```groovy
dependencies {
    def mini_version = "3.1.0"
    // Minimum working dependencies
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-android:$mini_version"
    // Use kapt as your annotation processor
    kapt "com.github.hyperdevs-team.mini-kotlin:mini-processor:$mini_version"
    // Or ksp if you prefer using Kotlin Symbol Processing (requires extra dependencies)
    ksp "com.github.hyperdevs-team.mini-kotlin:mini-processor:$mini_version"

    // Kodein helper libraries
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-kodein:$mini_version"
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-kodein-android:$mini_version"

    // Kodein helper library for view models scoped to the Navigation component's graph in Jetpack Compose
    implementation "com.github.hyperdevs-team.mini-kotlin:mini-kodein-android-compose:$mini_version"

    // Android Testing helper libraries
    androidTestImplementation "com.github.hyperdevs-team.mini-kotlin:mini-testing:$mini_version"
}
```

</details>

<details><summary>Kotlin</summary>

```kotlin
dependencies {
    val miniVersion = "3.1.0"
    // Minimum working dependencies
    implementation("com.github.hyperdevs-team.mini-kotlin:mini-android:$miniVersion")
    // Use kapt as your annotation processor
    kapt("com.github.hyperdevs-team.mini-kotlin:mini-processor:$miniVersion")
    // Or ksp if you prefer using Kotlin Symbol Processing (requires extra dependencies)
    ksp("com.github.hyperdevs-team.mini-kotlin:mini-processor:$miniVersion")

    // Kodein helper libraries
    implementation("com.github.hyperdevs-team.mini-kotlin:mini-kodein:$miniVersion")
    implementation("com.github.hyperdevs-team.mini-kotlin:mini-kodein-android:$miniVersion")

    // Kodein helper library for view models scoped to the Navigation component's graph in Jetpack Compose
    implementation("com.github.hyperdevs-team.mini-kotlin:mini-kodein-android-compose:$miniVersion")

    // Android Testing helper libraries
    androidTestImplementation("com.github.hyperdevs-team.mini-kotlin:mini-testing:$miniVersion")
}
```

</details>

If you want, you can also use *Kotlin Symbol Processing (KSP)* instead of KAPT. Keep in mind that
[KSP has some gotchas that can be worked around](#ksp-gotchas), so double check before using this.

<details><summary>KSP extra dependencies</summary>

<details open><summary>Groovy</summary>

Add this to your main `build.gradle`:

```groovy
buildscript {
    ext {
        ksp_version = <latest_ksp_version>
    }

    dependencies {
        classpath "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$ksp_version"
    }
}
```

And this to your module's `build.gradle`
```groovy
apply plugin: "com.google.devtools.ksp"

ksp "com.github.hyperdevs-team.mini-kotlin:mini-processor:$mini_version"
```

</details>

<details><summary>Kotlin</summary>

Add this to your main `build.gradle.kts`:
```kotlin
buildscript {
    dependencies {
        val kspVersion = <latest_ksp_version>
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${kspVersion}")
    }
}
```

And this to your module's `build.gradle.kts`
```groovy
plugins {
    id "com.google.devtools.ksp"
}

ksp("com.github.hyperdevs-team.mini-kotlin:mini-processor:${miniVersion}")
```

</details>

</details>

### JDK8 requirements
Ensure that your project has compatibility with Java 8:

For Kotlin projects:
```groovy 
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

For Android:
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

## Concepts
### Actions
An `Action` is a simple class that usually represents a use case. It can also contain a payload that includes data to perform said action. When an action is triggered, it will be delivered via `dispatcher` to the `stores` that are going to do something with the action to change their state.

For example, we may want to log in to a service. We would create an action like this one:
```kotlin
@Action
data class LoginAction(val username: String, val password: String)
```

When we receive the response from the server, we'll dispatch another action with the result:
```kotlin
@Action
data class LoginCompletedAction(val loginTask: Task, val user: User?)
```

Actions will usually be triggered from `Views`, `ViewModel`s or `Controllers`, depending on the architecture of your choice.

### Dispatcher
The `Dispatcher` is the hub that manages all data flow in a Flux application. It is basically a holder of store callbacks: each store registers itself and provides a callback for an action.

One important thing is that the dispatching is always performed in the same thread (usually the main thread) to avoid possible side-effects.

We can dispatch actions in the following ways:

```kotlin
// Dispatch an action in the main thread synchronously
dispatcher.dispatch(action = LoginAction(username = "user", password = "123"))

// Dispatch an action with the given scope
dispatcher.dispatchOn(action = LoginAction(username = "user", password = "123"), scope = coroutineScope)
```

### Store
The `Store`s are holders for application state and state mutation logic. In order to do so they expose pure reducer functions that are later invoked by the dispatcher. A `Store` is a type of a `StateContainer`, which is exactly that: a container of states.

A `State` is a plain object (usually a `data class`) that holds all information needed to display the view. States should always be immutable. State classes should avoid using framework-specific elements (View, Camera, Cursor...) in order to ease testing.

Stores subscribe to actions to change the application state after a `dispatch`. Mini generates the code that links dispatcher actions and stores using the `@Reducer` annotation over a **non-private function that receives an Action as parameter**.

```kotlin
data class SessionState(val loginTask: Task = taskIdle(), val loggedUser: User? = null)

class SessionStore(val controller: SessionController) : Store<SessionState>() {
    @Reducer
    fun login(action: LoginAction): SessionState {
        controller.login(action.username, action.password)
        return state.copy(loginTask = taskRunning(), loggedUser = null)
    }

    @Reducer
    fun onLoginCompleted(action: LoginCompletedAction): SessionState {
        return state.copy(loginTask = action.loginTask, loggedUser = action.user)
    }
}
```

### View changes
Each `StateContainer` exposes a Kotlin `Flow` that emits changes produced on the state, allowing a `View` or a `ViewModel` to listen to those changes and react accordingly to update the UI with the new `Store` state.

```kotlin  
mainStore.flow()
    .onEach { state ->
        // Do whatever you want
    }
    .launchIn(coroutineScope)
```  

### Tasks
A `Task` is a basic object to represent an ongoing process. They should be used in the state of our `StateContainer` (a `Store`, for example) to represent ongoing processes that must be represented in the UI.

You can also use `TypedTask` to save metadata related the current task. 

**IMPORTANT: Do not use TypedTask to hold values that must survive multiple task executions. Save them as a variable in the state instead.**

### Example
Given the example `Store`s and `Action`s explained before, the workflow would be:

- View dispatches `LoginAction`.
- Store changes the `loginTask` of its state to `loading` (or running state) and call a function in a `SessionController` to perform the login asynchronously.
- The view shows an Spinner when `loginTask` is in running state.
- The asynchronous call ends and `LoginCompletedAction` is dispatched, returning a null `User` and an error state `Task` if the asynchronous work failed or a success `Task` and a `User` if the work finished successfully.
- The Store changes its state to the given values from `LoginCompletedAction`.
- The view will react (for example, redirecting to another home view) if the task was success or shows an error if not.

You can execute another sample in the `app` package. It contains two different samples executing two types of `StateContainer`s:
- `StoreSampleActivity` class uses a `Store` as a `StateContainer`.
- `ViewModelSampleActivity` class uses a `ViewModel` as a `StateContainer`.

## How to use
### Setting up Mini
You'll need to add the following snippet to the class that initializes your application (for example, in Android you would set this in your `Application`'s `onCreate` method).

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

As soon as you do this, you'll have Mini up and running. You'll then need to declare your `Action`s, `Store`s and `State` as mentioned previously. The sample [app](app) contains examples regarding app configuration.

## Advanced usages
### Kotlin Flow Utils
Mini includes some utility extensions over Kotlin `Flow` to make easier listen state changes over the `StateContainer`s.

- `select`: Will emit only distinct values over the given `map` clause.
- `selectNotNull`: Like `select` but also avoiding null values.
- `onEachChange`: Emits a value when the values goes from one value to another.
- `onEachDisable`: Emits when the value goes from true to false.
- `onEachEnable`: Emits when the value goes from false to true.

You can see all extensions in [StoreFlow](mini-common/src/main/java/mini/StoreFlow.kt).

### Navigation and UI loops
In order to avoid loops when working with navigation based on a process result after dispatching an `Action`, you will need to do something like this

For example:
```kotlin
fun login(username: String, password: String) {
    dispatcher.dispatch(LoginAction(username, password))
    sessionStore.flow()
        .takeUntil { it.isTerminal }
        .onEach {
                // Do your stuff
        }
        .launchIn(coroutineScope)
}
```

### Merging state from multiple stores
Sometimes we want to use get data from multiple stores at the same time. You can do this by using `mergeStates`:

```kotlin
mergeStates<Any> {
        merge(userStore) { this }
        merge(downloadsStore) { this }
    }.select { (userState, downloadsState) ->
        CombinedState(userState, downloadState)
    }
        .onEach { 
            // Do your stuff
         }
        .llaunchIn(coroutineScope)
```

### Logging
Mini includes a custom `LoggerMiddleware` to log any change in your `StateContainer` states produced from an `Action`. This will allow you to keep track of your actions, changes and side-effects more easily. 
To add the `LoggerMiddleware` to your application you just need to add a single instance of it to your `Dispatcher`.
```kotlin
val loggerMiddleware = CustomLoggerMiddleware(stores().values)
dispatcher.addMiddleware(loggerMiddleware)
```

## Testing with Mini
Mini includes an extra library called mini-testing with a few methods and `TestRule`s to simplify your UI tests with this framework.

- `TestDispatcherRule` : This rule will intercept any action that arrives to the `Dispatcher`, avoiding any call to `Store`s. If we include this rule we will need to change the states manually in our tests.
- `CleanStateRule` : This rule resets the state of the `Store`s before and after each test.

Example of an Android test checking that an action is correctly dispatched:

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

Example of an Android test checking that a `View` correctly changes with an specific state:

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

## Kodein support
[Kodein](https://github.com/kosi-libs/Kodein) is a very simple and yet very useful dependency retrieval container. it is very easy to use and configure.

The library `mini-kodein` aims to ease working with Kodein and Mini by providing some utility methods to bind objects like `Store`s by relying on Kodein's retrieval capabilities.

```kotlin
object UserDIModule : BaseDIModule() {
    override val builder: DI.Builder.() -> Unit = {
        bindStore { UserStore(instance()) } // binds the store as a singleton and adds it to a seo of stores
        bind<UserController>() with singleton { UserControllerImpl(instance()) }
    }
}
```

## Android-specific features
### Proguard/R8
Each of the libraries contain a sensible proguard file that your project can consume in order to run you app on Proguard or R8.
No additional steps have to be done in order to use them apart from enabling minify in your project.

### Kodein Android utils
The library `mini-kodein-android` has some utility methods in order to inject an Android's `ViewModel` in a `DIAware` `Activity` or `Fragment`.
In order to use these methods, bind the Android's `ViewModelProvider.Factory` instance with Kodein:
```kotlin
// Use any tag to differ between the injected `Context` or `Application` if you are binding also `Context` with Kodein
bind<Application>("appTag") with singleton { app }
bind<ViewModelProvider.Factory>() with singleton { DIViewModelFactory(di.direct) }
```
To inject a `ViewModel` without parameters, bind it as follows:
```kotlin
bindViewModel { MainViewModel(instance("appTag") }
```
And in your `DIAware` `Activity` or `Fragment`:
```kotlin
private val mainViewModel: MainViewModel by viewModel()
```

### Kodein and Jetpack Compose utils
The library `mini-kodein-android-compose` has some utility methods in order to inject an Android's `ViewModel` in the scope of a Navigation component graph. This is useful as in Jetpack Compose it is common to have only one or few `Activities` and no `Fragment`s so, in order to scope the lifecycle of the `ViewModel` not for all the life of the `Activity`, we can scope it to any route existing in the `NavBackStackEntry`.

In order to use it, do the same as above, but instead of injecting the ViewModel scoped to a route of the Navigation, the `NavHost` composable must be inside an `DIAware Activity`, and then do as follows:
```kotlin
composable(route = "home") { navBackStackEntry ->
    val homeViewModel: HomeViewModel by navBackStackEntry.viewModel(contextDI())
    HomeScreen(homeViewModel, ...)
}
```
In case you want to pass an argument to a ViewModel, you need to bind the factory of that kind of Android's ViewModel.
You can do this in both `mini-kodein-android`, and `mini-kodein-android-compose`.
For example, given a `ViewModel` that you want to pass a `String` param, it would be:
```kotlin
bindViewModelFactory<HomeViewModelWithParameter, ViewModelProvider.Factory> { param ->
    TypedViewModelFactory(HomeViewModelWithParameter::class, instance("appTag"), param as String)
}
```
And to retrieve it with the given param in its constructor:
```kotlin
val param = "Hello World!"
val homeViewModelWithParameter: HomeViewModelWithParameter by navBackStackEntry.viewModel(contextDI(), param)
```

## Tips and tricks
### Improve compilation speed
In order to speed up the compilation process for `kapt`, it is recommended to add the following settings in
your `gradle.properties`:
```groovy
## Improves kapt speed with parallel annotation processing tasks, may impact in memory usage
kapt.use.worker.api=true
## Enables Gradle build cache
org.gradle.caching=true
```

## Known issues
### KSP gotchas
#### KSP code is not recognized by the IntelliJ IDEs
You may find that KSP generated sources are not indexed by IntelliJ IDEs. You can solve this by
declaring the proper source sets in your build.gradle:

For Android apps:
```groovy
applicationVariants.all { variant ->
    kotlin.sourceSets {
        def flavors = variant.productFlavors.indexed().collect { index, item ->
            def flavorName = item.name
            if (index > 0) return flavorName.capitalize() else flavorName
        }.join("")

        debug {
            getByName(flavors) {
                kotlin.srcDirs += "build/generated/ksp/${flavors}Debug/kotlin"
            }
        }

        release {
            getByName(flavors) {
                kotlin.srcDirs += "build/generated/ksp/${flavors}Release/kotlin"
            }
        }
    }
}
```

#### KSP code generates code for test source sets
You may encounter that KSP also runs over test source sets, so if you set any code related to Mini
in test sources, it will generate code that may override your main source set generated code.

A workaround to avoid this is to disable any KSP task for test source sets:
```groovy
afterEvaluate {
    tasks.matching {
        it.name.startsWith("ksp") && it.name.endsWith("TestKotlin")
    }.configureEach { it.enabled = false }
}
```

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
