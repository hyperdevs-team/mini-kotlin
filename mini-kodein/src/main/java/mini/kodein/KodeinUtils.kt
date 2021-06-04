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