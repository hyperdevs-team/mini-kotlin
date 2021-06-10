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

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class DispatcherTest {

    @Test
    fun `subscriptions are added`() {
        val dispatcher = newTestDispatcher()
        var called = 0
        dispatcher.subscribe<TestAction> {
            called++
        }
        dispatcher.dispatchBlocking(TestAction())
        called `should be equal to` 1
    }

    @Test
    fun `order is respected for same priority`() {
        val dispatcher = newTestDispatcher()
        val calls = ArrayList<Int>()
        dispatcher.subscribe<TestAction> {
            calls.add(0)
        }
        dispatcher.subscribe<TestAction> {
            calls.add(1)
        }
        dispatcher.dispatchBlocking(TestAction())
        calls[0] `should be equal to` 0
        calls[1] `should be equal to` 1
    }

    @Test
    fun `order is respected for different priority`() {
        val dispatcher = newTestDispatcher()
        val calls = ArrayList<Int>()
        dispatcher.subscribe<TestAction>(priority = 10) {
            calls.add(0)
        }
        dispatcher.subscribe<TestAction>(priority = 0) {
            calls.add(1)
        }
        dispatcher.dispatchBlocking(TestAction())
        calls[0] `should be equal to` 1
        calls[1] `should be equal to` 0
    }

    @Test
    fun `disposing registration removes subscription`() {
        val dispatcher = newTestDispatcher()
        var called = 0
        dispatcher.subscribe<TestAction> {
            called++
        }.close()
        dispatcher.dispatchBlocking(TestAction())
        called `should be equal to` 0
    }

    @Test
    fun `interceptors are called`() {
        val dispatcher = newTestDispatcher()
        var called = 0
        val interceptor = object : Middleware {
            override suspend fun intercept(action: Any, chain: Chain): Any {
                called++
                return chain.proceed(action)
            }
        }
        dispatcher.addMiddleware(interceptor)
        dispatcher.dispatchBlocking(TestAction())
        called `should be equal to` 1
    }
}