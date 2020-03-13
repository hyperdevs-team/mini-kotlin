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
