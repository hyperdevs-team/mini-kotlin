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

package mini.android.sample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import mini.*
import mini.android.FluxActivity
import mini.android.FluxStoreViewModel
import mini.android.sample.ui.theme.AppTheme

private val dispatcher = Dispatcher()

class MainViewModelReducer : NestedStateContainer<MainState>() {

    @Reducer
    fun handleLoading(state: MainState, action: SetLoadingAction): MainState {
        return state.copy(loading = action.loading)
    }

    @Reducer
    fun handleSetTextAction(state: MainState, action: SetTextAction): MainState {
        return state.copy(text = action.text)
    }
}

class MainStoreViewModel(savedStateHandle: SavedStateHandle) :
    FluxStoreViewModel<MainState>(savedStateHandle) {
    private val reducerSlice = MainViewModelReducer().apply { parent = this@MainStoreViewModel }

    init {
        Mini.link(dispatcher, listOf(this, reducerSlice)).track()
    }

    override fun saveState(state: MainState, handle: SavedStateHandle) {
        println("State saved")
        handle.set("state", state)
    }

    override fun restoreState(handle: SavedStateHandle): MainState? {
        val restored = handle.get<MainState>("state")
        println("State restored $restored")
        return restored
    }

    @UseCase
    suspend fun useCase(action: LongUseCaseAction) {
        if (state.loading) return
        dispatcher.dispatch(SetLoadingAction(true))
        delay(2000)
        dispatcher.dispatch(SetTextAction("${state.text.toInt() + 1}"))
        dispatcher.dispatch(SetLoadingAction(false))
    }
}

class ViewModelSampleActivity : FluxActivity() {

    private val viewModel: MainStoreViewModel by viewModels()

    override suspend fun whenCreated(savedInstanceState: Bundle?) {
        setContent {
            AppTheme {
                ViewModelSampleScreen()
            }
        }
    }

    @Composable
    private fun ViewModelSampleScreen() {
        val mainState = viewModel.flow().collectAsState(initial = MainState())
        val showToastState = viewModel.flow(hotStart = false).map { !it.loading }
            .collectAsState(initial = false)

        LaunchedEffect(showToastState.value) {
            if (showToastState.value) {
                Toast.makeText(
                    this@ViewModelSampleActivity,
                    "Finished loading",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SampleScreen(
                modifier = Modifier.padding(innerPadding),
                text = mainState.value.toString(),
                isLoading = mainState.value.loading,
                onStartSampleClicked = {
                    launch {
                        dispatcher.dispatch(LongUseCaseAction("HyperDevs"))
                    }
                }
            )
        }
    }
}
