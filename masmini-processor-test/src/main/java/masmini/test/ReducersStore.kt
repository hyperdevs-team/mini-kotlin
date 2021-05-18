package masmini.test

import kotlinx.coroutines.yield
import masmini.Reducer
import masmini.Store

class ReducersStore : Store<BasicState>() {

    companion object {
        @Reducer
        fun staticImpureReducer(action: AnyAction) {

        }

        @Reducer
        suspend fun staticSuspendingImpureReducer(action: AnyAction) {
            yield()
        }

        @Reducer
        fun staticPureReducer(state: BasicState, action: AnyAction): BasicState {
            return state.copy(value = action.value)
        }

        @Reducer
        suspend fun staticSuspendingPureReducer(state: BasicState, action: AnyAction): BasicState {
            yield()
            return state.copy(value = action.value)
        }
    }

    @Reducer
    fun impureReducer(action: AnyAction) {

    }

    @Reducer
    suspend fun impureSuspendingReducer(action: AnyAction) {
        yield()
    }

    @Reducer
    fun pureReducer(state: BasicState, action: AnyAction): BasicState {
        return state.copy(value = action.value)
    }

    @Reducer
    suspend fun pureSuspendingReducer(state: BasicState, action: AnyAction): BasicState {
        yield()
        return state.copy(value = action.value)
    }
}
