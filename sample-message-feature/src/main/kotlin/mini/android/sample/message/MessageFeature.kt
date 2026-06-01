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

package mini.android.sample.message

import mini.Action
import mini.Reducer
import mini.State
import mini.Store

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
