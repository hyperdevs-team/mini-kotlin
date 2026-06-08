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

package mini.android.sample.counter

import kotlinx.coroutines.CoroutineScope
import mini.Dispatcher
import mini.Mini
import mini.Action
import mini.Reducer
import mini.State
import mini.Store
import mini.codegen.Mini_Generated_counter_feature
import mini.flow
import java.io.Closeable

data class CounterState(val count: Int = 0) : State

@Action
data class IncrementCounterAction(val amount: Int = 1)

class CounterStore : Store<CounterState>() {
    @Reducer
    fun increment(state: CounterState, action: IncrementCounterAction): CounterState {
        return state.copy(count = state.count + action.amount)
    }
}

class CounterFeatureRuntime : Closeable {
    private val registry = Mini_Generated_counter_feature()
    private val dispatcher = Dispatcher()
    private val store = CounterStore()
    private val subscriptions = Mini.link(registry, dispatcher, store)

    init {
        store.initialize()
    }

    fun flow() = store.flow()

    fun increment(scope: CoroutineScope, amount: Int = 1) {
        dispatcher.dispatchOn(IncrementCounterAction(amount), scope)
    }

    override fun close() {
        subscriptions.close()
        store.close()
    }
}
