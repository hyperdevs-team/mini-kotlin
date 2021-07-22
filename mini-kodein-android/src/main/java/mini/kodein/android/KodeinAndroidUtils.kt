/*
 * Copyright 2021 HyperDevs
 *
 * Copyright 2020 BQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mini.kodein.android

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.*
import org.kodein.di.bindings.BindingDI
import org.kodein.di.bindings.NoArgBindingDI
import kotlin.reflect.KClass

/**
 * Binds a ViewModel to a Kotlin module, assuming that it's a provided dependency.
 */
inline fun <reified VM : ViewModel> DI.Builder.bindViewModel(overrides: Boolean? = null,
                                                             noinline creator: NoArgBindingDI<*>.() -> VM) {
    bind<VM>(VM::class.java.simpleName, overrides) with provider(creator)
}

/**
 * Binds a ViewModel factory to a Kotlin module in order to create new ViewModels.
 */
inline fun <reified VM : ViewModel, reified F : ViewModelProvider.Factory> DI.Builder.bindViewModelFactory(overrides: Boolean? = null,
                                                                                                           noinline creator: BindingDI<*>.(Any) -> F) {
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
class DIViewModelFactory(private val injector: DirectDI,
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
 * Injects a [ViewModel] into a [ComponentActivity] that implements [DIAware].
 */
@MainThread
inline fun <reified VM : ViewModel, A> A.viewModel(): Lazy<VM> where A : DIAware, A : ComponentActivity {
    return lazy {
        ViewModelProvider(this, direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [Fragment] that implements [DIAware].
 */
@MainThread
inline fun <reified VM : ViewModel, F> F.viewModel(): Lazy<VM> where F : DIAware, F : Fragment {
    return lazy {
        ViewModelProvider(this, direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [ComponentActivity] that implements [DIAware].
 *
 * Requires previous [ViewModelProvider.Factory] injection for the [ViewModel] via [bindViewModelFactory]
 * to work and a [TypedViewModel] to be used.
 */
@MainThread
inline fun <reified T : Any, reified VM : TypedViewModel<T>, A> A.viewModel(params: T): Lazy<VM> where A : DIAware, A : ComponentActivity {
    return lazy {
        ViewModelProvider(this, direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] into a [Fragment] that implements [DIAware].
 *
 * Requires previous [ViewModelProvider.Factory] injection for the [ViewModel] via [bindViewModelFactory]
 * to work and a [TypedViewModel] to be used.
 */
@MainThread
inline fun <reified T : Any, reified VM : TypedViewModel<T>, F> F.viewModel(params: T): Lazy<VM> where F : DIAware, F : Fragment {
    return lazy {
        ViewModelProvider(this, direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] with an [Activity] context that implements [DIAware
 * ], in order to share it between
 * different fragments hosted by that same [Activity].
 */
@MainThread
inline fun <reified VM : ViewModel, F> F.sharedActivityViewModel(): Lazy<VM> where F : DIAware, F : Fragment {
    return lazy {
        ViewModelProvider(this.requireActivity(), direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] with an [Activity] context that implements [DIAware], in order to share it between
 * different fragments hosted by that same [Activity].
 *
 * Requires previous [ViewModelProvider.Factory] injection for the [ViewModel] via [bindViewModelFactory]
 * to work and a [TypedViewModel] to be used.
 */
@MainThread
inline fun <reified T : Any, reified VM : TypedViewModel<T>, F> F.sharedActivityViewModel(params: T): Lazy<VM> where F : DIAware, F : Fragment {
    return lazy {
        ViewModelProvider(this.requireActivity(), direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Utility class so we don't need to create a [ViewModelProvider.Factory] for each [TypedViewModel]
 * in order to use [bindViewModelFactory].
 *
 * For injecting the factory of a specific view model, follow this scheme for each of your [TypedViewModel]]s:
 *
 * bindViewModelFactory<MyTypedViewModel, ViewModelProvider.Factory> { param ->
 *      TypedViewModelFactory(MyTypedViewModel::class, instance(App.KODEIN_APP_TAG), param as T)
 * }
 *
 * being T the type of the param of [MyTypedViewModel].
 */
class TypedViewModelFactory<T : TypedViewModel<*>>(private val typedViewModelKlass: KClass<T>,
                                                   private val app: Application,
                                                   private val params: Any) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM =
        typedViewModelKlass.constructors.first().call(app, params) as VM
}