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

package mini.flow

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mini.SampleStore
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Test

class FlowUtilsTest {

    @Test(timeout = 1000)
    fun `flow sends initial state on collection`(): Unit = runBlocking {
        val store = SampleStore()
        store.updateState("abc") //Set before collect
        var sentState = ""
        val job = GlobalScope.launch {
            store.flow(hotStart = true).take(1).collect {
                sentState = it
            }
        }
        job.join()
        sentState `should be equal to` "abc"
        Unit
    }

    @Test(timeout = 1000)
    fun `flow sends updates`(): Unit = runBlocking {
        val store = SampleStore()
        var sentState = ""
        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            store.flow(hotStart = false).take(1).collect {
                sentState = it
            }
        }
        store.updateState("abc")
        job.join()
        sentState `should be equal to` "abc"
        Unit
    }

    @Test(timeout = 1000)
    fun `flow sends updates to all`(): Unit = runBlocking {
        val store = SampleStore()
        val called = intArrayOf(0, 0)
        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            launch(start = CoroutineStart.UNDISPATCHED) {
                store.flow(hotStart = false).take(1).collect {
                    called[0]++
                }
            }
            launch(start = CoroutineStart.UNDISPATCHED) {
                store.flow(hotStart = false).take(1).collect {
                    called[1]++
                }
            }
        }
        store.updateState("abc")
        job.join()  //Wait for both to have their values
        //Each called two times, one for initial state, another for sent state
        called.`should equal`(intArrayOf(1, 1))
        Unit
    }

    @Test(timeout = 1000)
    fun `channel sends updates`(): Unit = runBlocking {
        val store = SampleStore()
        var sentState = ""
        val job = GlobalScope.launch {
            sentState = store.channel().receive()
        }
        store.updateState("abc")
        job.join()
        sentState `should be equal to` "abc"
        Unit
    }
}