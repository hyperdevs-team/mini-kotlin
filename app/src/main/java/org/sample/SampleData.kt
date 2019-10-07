package org.sample

import mini.Action
import mini.Reducer
import mini.Store

@Action
interface ActionInterface {
    val text: String
}

@Action
class ActionTwo(override val text: String) : ActionInterface

data class DummyState(val text: String = "dummy")
class DummyStore : Store<DummyState>() {
    @Reducer
    fun onActionTwo(action: ActionTwo) {
        newState = state.copy(text = action.text)
    }
}
