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

class StoreTest {

    @Test
    fun `state is updated`() {
        val store = SampleStore()
        store.setState("abc")
        store.state `should be equal to` "abc"
    }

    @Test
    fun `observers are called`() {
        val store = SampleStore()
        var state = ""
        store.subscribe {
            state = it
        }
        store.setState("abc")
        state `should be equal to` "abc"
    }

    @Test
    fun `initial state is sent on subscribe`() {
        val store = SampleStore()
        var state = ""
        store.subscribe {
            state = it
        }
        state `should be equal to` "initial"
    }

    @Test
    fun `observers are removed on close`() {
        val store = SampleStore()
        var state = ""
        val closeable = store.subscribe(hotStart = false) {
            state = it
        }
        closeable.close()
        store.setState("abc")
        state `should be equal to` ""
    }
}