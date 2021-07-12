/*
 * Copyright 2021 HyperDevs
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

package mini.kodein.android.compose

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import mini.kodein.android.TypedViewModel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Injects a [ViewModel] scoped to the lifecycle of the [NavBackStackEntry].
 * As [NavBackStackEntry] is a final class, can not implement [DIAware] so the [DI] is needed as
 * a param.
 */
@MainThread
inline fun <reified VM : ViewModel> NavBackStackEntry.viewModel(di: DI): Lazy<VM> {
    return lazy {
        ViewModelProvider(this, di.direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [TypedViewModel] scoped to the lifecycle of the [NavBackStackEntry].
 * As [NavBackStackEntry] is a final class, can not implement [DIAware] so the [DI] is needed as
 * a param.
 *
 * Requires previous ViewModelProvider.Factory injection for the ViewModel via bindViewModelFactory
 * to work and a TypedViewModel to be used.
 */
@MainThread
inline fun <reified T : Any, reified VM : TypedViewModel<T>> NavBackStackEntry.viewModel(di: DI, params: T): Lazy<VM> {
    return lazy {
        ViewModelProvider(this, di.direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}

/**
 * Injects a [ViewModel] scoped to the lifecycle of the [NavBackStackEntry] of the navigation
 * route given in [navigationRoute].
 * This allows to retrieve the same [ViewModel] from a previous route to which you have navigated in
 * the navigation graph instead of having a different [ViewModel] instance of the same [ViewModel]
 * for each navigation composable. It isn't needed that is from a nested navigation graph, only
 * that you have navigated previously to that route so we can find it in the [backStackEntry].
 * As [NavBackStackEntry] is a final class, can not implement [DIAware] so the [DI] is needed as
 * a param.
 */
@MainThread
inline fun <reified VM : ViewModel> NavController.sharedViewModelFromRoute(di: DI, navigationRoute: String): Lazy<VM> {
    return lazy {
        val parentBackStackEntry = getBackStackEntry(navigationRoute)
        ViewModelProvider(parentBackStackEntry, di.direct.instance()).get(VM::class.java)
    }
}

/**
 * Injects a [TypedViewModel] scoped to the lifecycle of the [NavBackStackEntry] of the navigation
 * route given in [navigationRoute].
 * This allows to retrieve the same [ViewModel] from a previous route to which you have navigated in
 * the navigation graph instead of having a different [ViewModel] instance of the same [ViewModel]
 * for each navigation composable. It isn't needed that is from a nested navigation graph, only
 * that you have navigated previously to that route so we can find it in the [backStackEntry].
 * As [NavBackStackEntry] is a final class, can not implement [DIAware] so the [DI] is needed as
 * a param.
 *
 * Requires previous ViewModelProvider.Factory injection for the ViewModel via bindViewModelFactory
 * to work and a TypedViewModel to be used.
 */
@MainThread
inline fun <reified T : Any, reified VM : TypedViewModel<T>> NavController.sharedViewModelFromRoute(di: DI,
                                                                                                    navigationRoute: String,
                                                                                                    params: T): Lazy<VM> {
    return lazy {
        val parentBackStackEntry = getBackStackEntry(navigationRoute)
        ViewModelProvider(parentBackStackEntry, di.direct.instance(VM::class.java, params)).get(VM::class.java)
    }
}