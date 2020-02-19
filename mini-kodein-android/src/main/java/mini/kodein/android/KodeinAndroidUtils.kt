package mini.kodein.android

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.bindings.BindingKodein
import org.kodein.di.bindings.NoArgBindingKodein
import org.kodein.di.direct
import org.kodein.di.generic.*

/**
 * Binds a ViewModel to a Kotlin module, assuming that it's a provided dependency.
 */
inline fun <reified VM : ViewModel> Kodein.Builder.bindViewModel(overrides: Boolean? = null,
                                                                 noinline creator: NoArgBindingKodein<*>.() -> VM) {
    bind<VM>(VM::class.java.simpleName, overrides) with provider(creator)
}

/**
 * Binds a ViewModel factory to a Kotlin module in order to create new ViewModels.
 */
inline fun <reified VM : ViewModel, reified F : ViewModelProvider.Factory> Kodein.Builder.bindViewModelFactory(overrides: Boolean? = null,
                                                                                                               noinline creator: BindingKodein<*>.(Any) -> F) {
    bind<F>(VM::class.java, overrides) with factory(creator = creator)
}

/**
 * [ViewModelProvider.Factory] implementation that relies in Kodein injector to retrieve ViewModel
 * instances.
 *
 * Optionally you can decide if you want all instances to be force-provided by module bindings or
 * if you allow creating new instances of them via [Class.newInstance] with [allowNewInstance].
 * The default is true to mimic the default behaviour of [ViewModelProvider].
 */
class KodeinViewModelFactory(private val injector: DKodein,
                             private val allowNewInstance: Boolean = true) : ViewModelProvider.Factory {
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
inline fun <reified VM : ViewModel, A> A.viewModel(): Lazy<VM> where A : KodeinAware, A : FragmentActivity {
    return lazy {
        ViewModelProvider(this, direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [Fragment] that implements [KodeinAware].
 */
@MainThread
inline fun <reified VM : ViewModel, F> F.viewModel(): Lazy<VM> where F : KodeinAware, F : Fragment {
    return lazy {
        ViewModelProvider(this, direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [FragmentActivity] that implements [KodeinAware].
 *
 * Requires previous [ViewModelProvider.Factory] injection for the [ViewModel] via [bindViewModelFactory]
 * to work and a [TypedViewModel] to be used.
 */
@MainThread
inline fun <reified T, reified VM : TypedViewModel<T>, A> A.viewModel(params: T): Lazy<VM> where A : KodeinAware, A : FragmentActivity {
    return lazy {
        ViewModelProvider(this, direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [Fragment] that implements [KodeinAware].
 *
 * Requires previous [ViewModelProvider.Factory] injection for the [ViewModel] via [bindViewModelFactory]
 * to work and a [TypedViewModel] to be used.
 */
@MainThread
inline fun <reified T, reified VM : TypedViewModel<T>, F> F.viewModel(params: T): Lazy<VM> where F : KodeinAware, F : Fragment {
    return lazy {
        ViewModelProvider(this, direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] with an [Activity] context that implements [KodeinAware], in order to share it between
 * different fragments hosted by that same [Activity].
 */
@MainThread
inline fun <reified VM : ViewModel, F> F.sharedActivityViewModel(): Lazy<VM> where F : KodeinAware, F : Fragment {
    return lazy {
        ViewModelProvider(this.requireActivity(), direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] with an [Activity] context that implements [KodeinAware], in order to share it between
 * different fragments hosted by that same [Activity].
 *
 * Requires previous [ViewModelProvider.Factory] injection for the [ViewModel] via [bindViewModelFactory]
 * to work and a [TypedViewModel] to be used.
 */
@MainThread
inline fun <reified T, reified VM : TypedViewModel<T>, F> F.sharedActivityViewModel(params: T): Lazy<VM> where F : KodeinAware, F : Fragment {
    return lazy {
        ViewModelProvider(this.requireActivity(), direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Generic [ViewModel] that adds support for adding a single [params] object to ease parameter
 * injection.
 */
open class TypedViewModel<T>(private val params: T) : ViewModel()