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
import org.junit.Test
import java.io.Closeable
import kotlin.reflect.KClass

class MiniTest {

    @Test
    fun `link initializes dispatcher action types from explicit registry`() {
        val dispatcher = Dispatcher()
        val store = SampleStore()
        val registry = TestRegistry(
            actionTypes = mapOf(TestAction::class to listOf(TestAction::class, Any::class))
        )

        Mini.link(registry, dispatcher, store).close()

        dispatcher.actionTypeMap[TestAction::class] `should be equal to` listOf(TestAction::class, Any::class)
    }

    @Test
    fun `link delegates subscriptions only to explicit registry`() {
        val dispatcher = Dispatcher()
        val store = SampleStore()
        val registry = TestRegistry(emptyMap())

        Mini.link(registry, dispatcher, store).close()

        registry.subscriptionCount `should be equal to` 1
    }

    @Test
    fun `link supports multiple containers with one local registry`() {
        val dispatcher = Dispatcher()
        val firstStore = SampleStore()
        val secondStore = SampleStore()
        val registry = TestRegistry(emptyMap())

        Mini.link(registry, dispatcher, listOf(firstStore, secondStore)).close()

        registry.subscriptionCount `should be equal to` 2
    }

    @Test
    fun `link does not override a dispatcher action map that is already initialized`() {
        val existingActionMap: Map<KClass<*>, List<KClass<*>>> = mapOf(
            TestAction::class to listOf(State::class)
        )
        val dispatcher = Dispatcher().apply {
            actionTypeMap = existingActionMap
        }
        val store = SampleStore()
        val registry = TestRegistry(
            actionTypes = mapOf(TestAction::class to listOf(TestAction::class, Any::class))
        )

        Mini.link(registry, dispatcher, store).close()

        dispatcher.actionTypeMap `should be equal to` existingActionMap
    }

    private class TestRegistry(
        override val actionTypes: Map<KClass<*>, List<KClass<*>>>
    ) : MiniRegistry {
        var subscriptionCount = 0

        override fun <S : State> subscribe(dispatcher: Dispatcher, container: StateContainer<S>): Closeable {
            subscriptionCount++
            return Closeable { }
        }
    }
}
