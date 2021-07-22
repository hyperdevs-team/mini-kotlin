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
import kotlin.reflect.KClass

const val DISPATCHER_FACTORY_CLASS_NAME = "mini.codegen.Mini_Generated"

abstract class Mini {

    companion object {

        private val miniInstance: Mini by lazy {
            try {
                Class.forName(DISPATCHER_FACTORY_CLASS_NAME).getField("INSTANCE").get(null) as Mini
            } catch (ex: Throwable) {
                throw ClassNotFoundException("Failed to load generated class $DISPATCHER_FACTORY_CLASS_NAME, " +
                                             "most likely kapt did not run, add it as dependency to the project", ex)
            }
        }

        /**
         * Generate all subscriptions from @[Reducer] annotated methods and bundle
         * into a single Closeable.
         */
        fun link(dispatcher: Dispatcher, container: StateContainer<*>): Closeable {
            ensureDispatcherInitialized(dispatcher)
            return miniInstance.subscribe(dispatcher, container)
        }

        /**
         * Generate all subscriptions from @[Reducer] annotated methods and bundle
         * into a single Closeable.
         */
        fun link(dispatcher: Dispatcher, containers: Iterable<StateContainer<*>>): Closeable {
            ensureDispatcherInitialized(dispatcher)
            return miniInstance.subscribe(dispatcher, containers)
        }

        private fun ensureDispatcherInitialized(dispatcher: Dispatcher) {
            if (dispatcher.actionTypeMap.isEmpty()) {
                dispatcher.actionTypeMap = miniInstance.actionTypes
            }
        }

    }

    /**
     * All the types an action can be subscribed as.
     */
    abstract val actionTypes: Map<KClass<*>, List<KClass<*>>>

    /**
     * Link all [Reducer] functions present in the store to the dispatcher.
     */
    protected abstract fun <S> subscribe(dispatcher: Dispatcher, container: StateContainer<S>): Closeable

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