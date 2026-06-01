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

package mini.multiregistry

import kotlinx.coroutines.runBlocking
import mini.Dispatcher
import mini.Mini
import mini.ksptest.KspAnyAction
import mini.ksptest.KspReducersStore
import mini.test.AnyAction
import mini.test.ReducersStore
import org.amshove.kluent.`should be equal to`
import org.junit.Assert.assertTrue
import org.junit.Test

internal class MultiRegistryIntegrationTest {

    private val kaptStore = ReducersStore()
    private val kspStore = KspReducersStore()
    private val dispatcher = Dispatcher().apply {
        Mini.link(this, listOf(kaptStore, kspStore))
    }

    @Test
    fun `action type map merges registries from different modules`() {
        assertTrue(dispatcher.actionTypeMap.containsKey(AnyAction::class))
        assertTrue(dispatcher.actionTypeMap.containsKey(KspAnyAction::class))
    }

    @Test
    fun `reducers from multiple generated registries are invoked on one dispatcher`() {
        runBlocking {
            dispatcher.dispatch(AnyAction("kapt-changed"))
            dispatcher.dispatch(KspAnyAction("ksp-changed"))

            kaptStore.state.value `should be equal to` "kapt-changed"
            kspStore.state.value `should be equal to` "ksp-changed"
        }
    }
}
