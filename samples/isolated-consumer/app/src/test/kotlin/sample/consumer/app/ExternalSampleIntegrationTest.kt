/*
 * Copyright 2026 HyperDevs
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

package sample.consumer.app

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import sample.consumer.message.MessageFeatureRuntime

class ExternalSampleIntegrationTest {

    @Test
    fun `external consumer can use message feature with its own local mini runtime`() {
        runBlocking {
            MessageFeatureRuntime().use { runtime ->
                runtime.advance()
                runtime.state.text `should be equal to` "message-1"

                runtime.setMessage("external-consumer")
                runtime.state.text `should be equal to` "external-consumer"
            }
        }
    }
}
