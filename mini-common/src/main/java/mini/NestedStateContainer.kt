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

package mini

import java.io.Closeable

/**
 * Utility class to allow splitting [StateContainer] into chunks so not all reducers live in the same
 * file.
 *
 * From a state container
 *
 * ```
 * class Reducer : NestedStateContainer<State>() {
 *      @Reducer
 *      fun reduceOneAction(...)
 * }
 *
 * class MyStore {
 *      val reducer = Reducer(this)
 *
 *      init {
 *          Mini.link(dispatcher, listOf(this, reducer))
 *      }
 *
 *      @Reducer
 *      fun globalReduceFn(...)
 * }
 * ```
 */
abstract class NestedStateContainer<S : Any>(var parent: StateContainer<S>? = null) : StateContainer<S> {
    override val state: S
        get() = parent!!.state

    override fun setState(newState: S) {
        parent!!.setState(newState)
    }

    override fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        return parent!!.subscribe(hotStart, fn)
    }
}