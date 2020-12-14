package mini.kodein

import mini.Store
import org.kodein.di.*
import org.kodein.di.bindings.NoArgBindingDI

/**
 * Work based on: https://proandroiddev.com/android-viewmodel-dependency-injection-with-kodein-249f80f083c9
 */

/**
 * Binds a store in a Kodein module, assuming that it's a singleton dependency.
 */
inline fun <reified T : Store<*>> DI.Builder.bindStore(noinline creator: NoArgBindingDI<*>.() -> T) {
    bind<T>() with singleton(creator = creator)
    bind<Store<*>>().inSet() with singleton { instance<T>() }
}