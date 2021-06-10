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

package mini.android

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import mini.CloseableTracker
import mini.DefaultCloseableTracker
import mini.StateContainer
import mini.assertOnUiThread
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

abstract class FluxStoreViewModel<S : Any>(
    val savedStateHandle: SavedStateHandle) :
    ViewModel(),
    StateContainer<S>,
    CloseableTracker by DefaultCloseableTracker() {

    class ViewModelSubscription internal constructor(private val vm: FluxStoreViewModel<*>,
                                                     private val fn: Any) : Closeable {
        override fun close() {
            vm.listeners.remove(fn)
        }
    }

    private var _state: Any? = StateContainer.Companion.NoState
    private val listeners = CopyOnWriteArrayList<(S) -> Unit>()

    override val state: S
        get() {
            if (_state === StateContainer.Companion.NoState) {
                synchronized(this) {
                    if (_state === StateContainer.Companion.NoState) {
                        _state = restoreState(savedStateHandle) ?: initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }

    override fun setState(newState: S) {
        assertOnUiThread()
        performStateChange(newState)
    }

    private fun performStateChange(newState: S) {
        if (_state != newState) {
            _state = newState
            saveState(newState, savedStateHandle)
            listeners.forEach {
                it(newState)
            }
        }
    }

    /**
     * Persist the state, no-op by default.
     *
     * ```handle.set("state", state)```
     */
    open fun saveState(state: S, handle: SavedStateHandle) {
        //No-op
    }

    /**
     * Restore the state from the [SavedStateHandle] or null if nothing was saved.
     *
     * ```handle.get<S>("state")```
     */
    open fun restoreState(handle: SavedStateHandle): S? {
        return null
    }

    override fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return ViewModelSubscription(this, fn)
    }

    override fun onCleared() {
        super.onCleared()
        close()
    }
}