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

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import mini.SampleStore.Companion.INITIAL_STATE
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Test
import java.util.concurrent.Executors

class StoreFlowTest {

    private val testScope =
        CoroutineScope(Executors.newScheduledThreadPool(1).asCoroutineDispatcher())

    @Test(timeout = 1000)
    fun `flow sends initial state on collection`(): Unit = runBlocking {
        val store = SampleStore()
        var observedState = SampleState(INITIAL_STATE)

        val job = store.flow(hotStart = false)
            .onEach { observedState = it }
            .take(1)
            .launchIn(testScope)

        store.setState(SampleState("abc")) //Set before collect

        job.join()
        observedState `should be equal to` SampleState("abc")
        Unit
    }

    @Test(timeout = 1000)
    fun `flow sends updates to all`(): Unit = runBlocking {
        val store = SampleStore()
        val called = intArrayOf(0, 0)

        val job1 = store.flow()
            .onEach { called[0]++ }
            .take(2)
            .launchIn(testScope)

        val job2 = store.flow()
            .onEach { called[1]++ }
            .take(2)
            .launchIn(testScope)

        store.setState(SampleState("abc"))

        job1.join()
        job2.join()

        //Called two times, one for initial state, one for updated stated
        called.`should be equal to`(intArrayOf(2, 2))
        Unit
    }

    @Test(timeout = 1000)
    fun `channel sends updates`(): Unit = runBlocking {
        val store = SampleStore()
        var observedState = SampleState("")
        val scope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        val job = scope.launch {
            observedState = store.channel().receive()
        }
        store.setState(SampleState("abc"))
        job.join()
        observedState `should be equal to` SampleState("abc")
        Unit
    }

    @Test(timeout = 1000)
    fun `flow closes`(): Unit = runBlocking {
        val store = SampleStore()
        var observedState = store.state

        val scope = CoroutineScope(Job())
        store.flow()
            .onEach {
                observedState = it
            }
            .launchIn(scope)

        scope.cancel() //Cancel the scope
        store.setState(SampleState("abc"))

        observedState `should be equal to` SampleState(INITIAL_STATE)
        Unit
    }
}