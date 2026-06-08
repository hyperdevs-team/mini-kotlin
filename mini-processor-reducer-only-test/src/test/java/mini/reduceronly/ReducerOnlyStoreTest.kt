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

package mini.reduceronly

import kotlinx.coroutines.runBlocking
import mini.Dispatcher
import mini.Mini
import mini.codegen.Mini_Generated_reducer_only_test
import mini.test.AnyAction
import org.amshove.kluent.`should be equal to`
import org.junit.Test

internal class ReducerOnlyStoreTest {

    private val store = ReducerOnlyStore()
    private val dispatcher = Dispatcher().apply {
        Mini.link(Mini_Generated_reducer_only_test(), this, listOf(store))
    }

    @Test
    fun `reducers are generated without local actions`() {
        runBlocking {
            dispatcher.dispatch(AnyAction("changed"))
            store.state.value `should be equal to` "changed"
        }
    }
}
