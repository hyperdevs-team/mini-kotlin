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

import org.amshove.kluent.`should not be empty`
import org.junit.Test

class LoggerMiddlewareTest {

    @Test
    fun `logs are printed`() {
        val store = SampleStore()
        val dispatcher = newTestDispatcher()
        dispatcher.subscribe<TestAction> {
            store.setState("Action sent")
        }

        val out = StringBuilder()
        dispatcher.addMiddleware(LoggerMiddleware(listOf(store),
            logger = { priority, tag, msg ->
                println("[$priority][$tag] $msg")
                out.append(priority).append(tag).append(msg)
            }))
        dispatcher.dispatchBlocking(TestAction())
        out.toString().`should not be empty`()
    }

}