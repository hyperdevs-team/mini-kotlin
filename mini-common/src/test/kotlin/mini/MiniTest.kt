/*
 * Copyright 2024 HyperDevs
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

import org.amshove.kluent.`should be equal to`
import org.junit.After
import org.junit.Test
import java.io.Closeable
import kotlin.reflect.KClass

class MiniTest {

    @After
    fun tearDown() {
        MiniRuntime.reset()
    }

    @Test
    fun `link initializes dispatcher action types from all registries`() {
        val dispatcher = Dispatcher()
        val store = SampleStore()
        MiniRuntime.loadRegistries = {
            listOf(
                TestRegistry(
                    actionTypes = mapOf(TestAction::class to listOf(TestAction::class, Any::class)),
                    accepts = setOf(SampleStore::class)
                ),
                TestRegistry(
                    actionTypes = mapOf(TestAction::class to listOf(TestAction::class, State::class)),
                    accepts = emptySet()
                )
            )
        }

        Mini.link(dispatcher, store).close()

        dispatcher.actionTypeMap[TestAction::class] `should be equal to` listOf(TestAction::class, Any::class, State::class)
    }

    @Test
    fun `link delegates subscriptions to all registries`() {
        val dispatcher = Dispatcher()
        val store = SampleStore()
        val appRegistry = TestRegistry(emptyMap(), accepts = setOf(SampleStore::class))
        val unrelatedRegistry = TestRegistry(emptyMap(), accepts = emptySet())
        MiniRuntime.loadRegistries = { listOf(appRegistry, unrelatedRegistry) }

        Mini.link(dispatcher, store).close()

        appRegistry.subscriptionCount `should be equal to` 1
        unrelatedRegistry.subscriptionCount `should be equal to` 1
    }

    @Test
    fun `link falls back to legacy registry when no new registries are present`() {
        val dispatcher = Dispatcher()
        val store = SampleStore()
        val legacyRegistry = TestRegistry(
            actionTypes = mapOf(TestAction::class to listOf(TestAction::class)),
            accepts = setOf(SampleStore::class)
        )
        MiniRuntime.loadRegistries = { emptyList() }
        MiniRuntime.loadLegacyRegistry = { legacyRegistry }

        Mini.link(dispatcher, store).close()

        dispatcher.actionTypeMap[TestAction::class] `should be equal to` listOf(TestAction::class)
        legacyRegistry.subscriptionCount `should be equal to` 1
    }

    private class TestRegistry(
        override val actionTypes: Map<KClass<*>, List<KClass<*>>>,
        private val accepts: Set<KClass<*>>
    ) : MiniRegistry {
        var subscriptionCount = 0

        override fun <S : State> subscribe(dispatcher: Dispatcher, container: StateContainer<S>): Closeable {
            subscriptionCount++
            return Closeable { }
        }
    }
}
