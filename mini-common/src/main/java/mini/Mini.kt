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

package mini

import java.io.Closeable

abstract class Mini : MiniRegistry {

    companion object {

        /**
         * Generate all subscriptions from @[Reducer] annotated methods and bundle
         * into a single Closeable.
         */
        fun link(registry: MiniRegistry,
                 dispatcher: Dispatcher,
                 container: StateContainer<*>): Closeable {
            ensureDispatcherInitialized(registry, dispatcher)
            val c = CompositeCloseable()
            c.add(registry.subscribe(dispatcher, container))
            return c
        }

        /**
         * Generate all subscriptions from @[Reducer] annotated methods and bundle
         * into a single Closeable.
         */
        fun link(registry: MiniRegistry,
                 dispatcher: Dispatcher,
                 containers: Iterable<StateContainer<*>>): Closeable {
            ensureDispatcherInitialized(registry, dispatcher)
            val c = CompositeCloseable()
            containers.forEach { container ->
                c.add(registry.subscribe(dispatcher, container))
            }
            return c
        }

        private fun ensureDispatcherInitialized(registry: MiniRegistry, dispatcher: Dispatcher) {
            if (dispatcher.actionTypeMap.isEmpty()) {
                dispatcher.actionTypeMap = registry.actionTypes
            }
        }

    }

    /**
     * Link all [Reducer] functions present in the store to the dispatcher.
     */
    abstract override fun <S: State> subscribe(dispatcher: Dispatcher,
                                               container: StateContainer<S>): Closeable

    /**
     * Link all [Reducer] functions present in the store to the dispatcher.
     */
    protected fun subscribe(dispatcher: Dispatcher, containers: Iterable<StateContainer<*>>): Closeable {
        val c = CompositeCloseable()
        containers.forEach { container ->
            c.add(subscribe(dispatcher, container))
        }
        return c
    }
}
