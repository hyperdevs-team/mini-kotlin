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

package mini

import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Basic state holder.
 */
abstract class Store<S> : Closeable,
    StateContainer<S>,
    CloseableTracker by DefaultCloseableTracker() {

    class StoreSubscription internal constructor(private val store: Store<*>,
                                                 private val fn: Any) : Closeable {
        override fun close() {
            store.listeners.remove(fn)
        }
    }

    private var _state: Any? = StateContainer.Companion.NoState
    private val listeners = CopyOnWriteArrayList<(S) -> Unit>()

    /**
     * Initialize the store after dependency injection is complete.
     */
    open fun initialize() {
        //No-op
    }

    /**
     * Set new state and notify listeners, only callable from the main thread.
     */
    override fun setState(newState: S) {
        assertOnUiThread()
        performStateChange(newState)
    }

    override fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return StoreSubscription(this, fn)
    }

    override val state: S
        get() {
            if (_state === StateContainer.Companion.NoState) {
                synchronized(this) {
                    if (_state === StateContainer.Companion.NoState) {
                        _state = initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }

    private fun performStateChange(newState: S) {
        //State mutation should to happen on UI thread
        if (_state != newState) {
            _state = newState
            listeners.forEach {
                it(newState)
            }
        }
    }

    override fun close() {
        listeners.clear() //Remove all listeners
    }

}
