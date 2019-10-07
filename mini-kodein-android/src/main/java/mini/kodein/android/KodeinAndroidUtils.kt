package mini.kodein.android

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.bindings.NoArgBindingKodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.instanceOrNull
import org.kodein.di.generic.provider

/**
 * Binds a ViewModel to a Kotlin module, assuming that it's a provided dependency.
 */
inline fun <reified T : ViewModel> Kodein.Builder.bindViewModel(overrides: Boolean? = null, noinline creator: NoArgBindingKodein<*>.() -> T) {
    bind<T>(T::class.java.simpleName, overrides) with provider(creator)
}

/**
 * [ViewModelProvider.Factory] implementation that relies in Kodein injector to retrieve ViewModel
 * instances.
 *
 * Optionally you can decide if you want all instances to be force-provided by module bindings or
 * if you allow creating new instances of them via [Class.newInstance] with [allowNewInstance].
 * The default is true to mimic the default behaviour of [ViewModelProviders.of].
 */
class KodeinViewModelFactory(
    private val injector: DKodein,
    private val allowNewInstance: Boolean = true
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return injector.instanceOrNull<ViewModel>(tag = modelClass.simpleName) as T?
            ?: if (allowNewInstance) {
                modelClass.newInstance()
            } else {
                throw RuntimeException("The class ${modelClass.name} cannot be provided as no Kodein bindings could be found")
            }
    }
}

/**
 * Injects a [ViewModel] into a [FragmentActivity] that implements [KodeinAware].
 */
@MainThread
inline fun <reified VM : ViewModel, T> T.viewModel(): Lazy<VM> where T : KodeinAware, T : FragmentActivity {
    return lazy {
        ViewModelProviders.of(this, direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [Fragment] that implements [KodeinAware].
 */
@MainThread
inline fun <reified VM : ViewModel, T> T.viewModel(): Lazy<VM> where T : KodeinAware, T : Fragment {
    return lazy {
        ViewModelProviders.of(this, direct.instance()).get(VM::class.java)
    }
}