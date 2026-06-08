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

package mini.android.sample.message

import kotlinx.coroutines.CoroutineScope
import mini.Dispatcher
import mini.Mini
import mini.Action
import mini.Reducer
import mini.State
import mini.Store
import mini.codegen.Mini_Generated_message_feature
import mini.flow
import java.io.Closeable

data class MessageState(
    val text: String = "idle",
    val version: Int = 0
) : State

@Action
data class SetMessageAction(val value: String)

@Action
data class AdvanceMessageAction(val prefix: String = "message")

class MessageStore : Store<MessageState>() {
    @Reducer
    fun setMessage(state: MessageState, action: SetMessageAction): MessageState {
        return state.copy(text = action.value)
    }

    @Reducer
    fun advanceMessage(state: MessageState, action: AdvanceMessageAction): MessageState {
        val nextVersion = state.version + 1
        return state.copy(
            text = "${action.prefix}-$nextVersion",
            version = nextVersion
        )
    }
}

class MessageFeatureRuntime : Closeable {
    private val registry = Mini_Generated_message_feature()
    private val dispatcher = Dispatcher()
    private val store = MessageStore()
    private val subscriptions = Mini.link(registry, dispatcher, store)

    init {
        store.initialize()
    }

    fun flow() = store.flow()

    fun advance(scope: CoroutineScope, prefix: String = "message") {
        dispatcher.dispatchOn(AdvanceMessageAction(prefix), scope)
    }

    fun setMessage(scope: CoroutineScope, value: String) {
        dispatcher.dispatchOn(SetMessageAction(value), scope)
    }

    override fun close() {
        subscriptions.close()
        store.close()
    }
}
