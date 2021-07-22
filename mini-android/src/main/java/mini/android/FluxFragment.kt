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

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import mini.CloseableTracker
import mini.DefaultCloseableTracker
import kotlin.coroutines.CoroutineContext

abstract class FluxFragment : Fragment(),
    CloseableTracker by DefaultCloseableTracker(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.launch { whenCreated(savedInstanceState) }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { whenResumed() }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch { whenPaused() }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch { whenStopped() }
    }

    override fun onDestroy() {
        lifecycleScope.launch { whenDestroyed() }
        close()
        super.onDestroy()
    }

    fun <T> Flow<T>.launchOnUi() {
        launchIn(lifecycleScope)
    }

    protected open suspend fun whenCreated(savedInstanceState: Bundle?) = Unit
    protected open suspend fun whenResumed() = Unit
    protected open suspend fun whenPaused() = Unit
    protected open suspend fun whenStopped() = Unit
    protected open suspend fun whenDestroyed() = Unit
}