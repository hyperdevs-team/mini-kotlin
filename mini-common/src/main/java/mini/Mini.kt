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
import java.util.ServiceLoader
import kotlin.reflect.KClass

const val DISPATCHER_FACTORY_CLASS_NAME = "mini.codegen.Mini_Generated"

internal object MiniRuntime {
    var loadRegistries: () -> List<MiniRegistry> = {
        ServiceLoader.load(MiniRegistry::class.java, Mini::class.java.classLoader).iterator().asSequence().toList()
    }

    var loadLegacyRegistry: () -> MiniRegistry = {
        try {
            Class.forName(DISPATCHER_FACTORY_CLASS_NAME).getField("INSTANCE").get(null) as MiniRegistry
        } catch (ex: Throwable) {
            throw ClassNotFoundException("Failed to load generated class $DISPATCHER_FACTORY_CLASS_NAME, " +
                                         "most likely the annotation processor did not run, add it as dependency to the project", ex)
        }
    }

    fun reset() {
        loadRegistries = {
            ServiceLoader.load(MiniRegistry::class.java, Mini::class.java.classLoader).iterator().asSequence().toList()
        }
        loadLegacyRegistry = {
            try {
                Class.forName(DISPATCHER_FACTORY_CLASS_NAME).getField("INSTANCE").get(null) as MiniRegistry
            } catch (ex: Throwable) {
                throw ClassNotFoundException("Failed to load generated class $DISPATCHER_FACTORY_CLASS_NAME, " +
                                             "most likely the annotation processor did not run, add it as dependency to the project", ex)
            }
        }
    }
}

abstract class Mini : MiniRegistry {

    companion object {

        private fun registries(): List<MiniRegistry> {
            val registries = MiniRuntime.loadRegistries()
            return if (registries.isNotEmpty()) registries else listOf(MiniRuntime.loadLegacyRegistry())
        }

        /**
         * Generate all subscriptions from @[Reducer] annotated methods and bundle
         * into a single Closeable.
         */
        fun link(dispatcher: Dispatcher, container: StateContainer<*>): Closeable {
            ensureDispatcherInitialized(dispatcher)
            val c = CompositeCloseable()
            val registries = registries()
            registries.forEach { registry ->
                c.add(registry.subscribe(dispatcher, container))
            }
            return c
        }

        /**
         * Generate all subscriptions from @[Reducer] annotated methods and bundle
         * into a single Closeable.
         */
        fun link(dispatcher: Dispatcher, containers: Iterable<StateContainer<*>>): Closeable {
            ensureDispatcherInitialized(dispatcher)
            val c = CompositeCloseable()
            val registries = registries()
            containers.forEach { container ->
                registries.forEach { registry ->
                    c.add(registry.subscribe(dispatcher, container))
                }
            }
            return c
        }

        private fun ensureDispatcherInitialized(dispatcher: Dispatcher) {
            if (dispatcher.actionTypeMap.isEmpty()) {
                dispatcher.actionTypeMap = mergeActionTypes(registries())
            }
        }

        private fun mergeActionTypes(registries: List<MiniRegistry>): Map<KClass<*>, List<KClass<*>>> {
            return registries
                .asSequence()
                .flatMap { it.actionTypes.asSequence() }
                .groupBy({ it.key }, { it.value })
                .mapValues { (_, values) -> values.flatten().distinct() }
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
        val registries = registries()
        containers.forEach { container ->
            registries.forEach { registry ->
                c.add(registry.subscribe(dispatcher, container))
            }
        }
        return c
    }
}
