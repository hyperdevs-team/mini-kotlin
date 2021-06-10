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

package mini.test

import kotlinx.coroutines.yield
import mini.Reducer
import mini.Store

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
