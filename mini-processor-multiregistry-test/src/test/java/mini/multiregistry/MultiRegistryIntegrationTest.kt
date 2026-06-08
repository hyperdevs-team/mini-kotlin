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

package mini.multiregistry

import kotlinx.coroutines.runBlocking
import mini.Dispatcher
import mini.Mini
import mini.codegen.Mini_Generated_processor_ksp_test
import mini.codegen.Mini_Generated_processor_test
import mini.ksptest.KspAnyAction
import mini.ksptest.KspReducersStore
import mini.test.AnyAction
import mini.test.ReducersStore
import org.amshove.kluent.`should be equal to`
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class MultiRegistryIntegrationTest {

    private val kaptStore = ReducersStore()
    private val kspStore = KspReducersStore()
    private val kaptDispatcher = Dispatcher().apply {
        Mini.link(Mini_Generated_processor_test(), this, listOf(kaptStore))
    }
    private val kspDispatcher = Dispatcher().apply {
        Mini.link(Mini_Generated_processor_ksp_test(), this, listOf(kspStore))
    }

    @Test
    fun `explicit local registries keep action maps isolated across modules`() {
        assertTrue(kaptDispatcher.actionTypeMap.containsKey(AnyAction::class))
        assertFalse(kaptDispatcher.actionTypeMap.containsKey(KspAnyAction::class))
        assertTrue(kspDispatcher.actionTypeMap.containsKey(KspAnyAction::class))
        assertFalse(kspDispatcher.actionTypeMap.containsKey(AnyAction::class))
    }

    @Test
    fun `generated registries from different modules coexist without collisions`() {
        runBlocking {
            kaptDispatcher.dispatch(AnyAction("kapt-changed"))
            kspDispatcher.dispatch(KspAnyAction("ksp-changed"))

            kaptStore.state.value `should be equal to` "kapt-changed"
            kspStore.state.value `should be equal to` "ksp-changed"
        }
    }
}
