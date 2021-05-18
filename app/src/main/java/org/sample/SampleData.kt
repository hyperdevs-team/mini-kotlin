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

package org.sample

import mini.Action
import mini.Reducer
import mini.Store

@Action
interface ActionInterface {
    val text: String
}

@Action
class ActionOne(override val text: String) : ActionInterface

@Action
class ActionTwo(override val text: String) : ActionInterface

data class DummyState(val text: String = "dummy")
class DummyStore : Store<DummyState>() {
    @Reducer
    fun onActionOne(action: ActionOne) {
        newState = state.copy(text = "${state.text}  ${action.text}")
    }

    @Reducer
    fun onActionTwo(action: ActionTwo) {
        newState = state.copy(text = action.text)
    }
}
