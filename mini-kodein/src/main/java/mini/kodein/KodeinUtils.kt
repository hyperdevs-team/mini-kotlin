package mini.kodein

import mini.Store
import org.kodein.di.Kodein
import org.kodein.di.bindings.NoArgSimpleBindingKodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

/**
 * Work based on: https://proandroiddev.com/android-viewmodel-dependency-injection-with-kodein-249f80f083c9
 */

/**
 * Binds a store in a Kodein module, assuming that it's a singleton dependency.
 */
inline fun <reified T : Store<*>> Kodein.Builder.bindStore(noinline creator: NoArgSimpleBindingKodein<*>.() -> T) {
    bind<T>() with singleton(creator = creator)
    bind<Store<*>>().inSet() with singleton { instance<T>() }
}