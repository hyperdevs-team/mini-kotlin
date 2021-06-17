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

package com.example.androidsample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import mini.*
import mini.android.FluxActivity
import mini.android.FluxStoreViewModel

private val dispatcher = Dispatcher()

class MainViewModelReducer : NestedStateContainer<State>() {

    @Reducer
    fun handleLoading(state: State, action: SetLoadingAction): State {
        return state.copy(loading = action.loading)
    }

    @Reducer
    fun handleSetTextAction(state: State, action: SetTextAction): State {
        return state.copy(text = action.text)
    }
}

class MainStoreViewModel(savedStateHandle: SavedStateHandle) : FluxStoreViewModel<State>(savedStateHandle) {
    private val reducerSlice = MainViewModelReducer().apply { parent = this@MainStoreViewModel }

    init {
        Mini.link(dispatcher, listOf(this, reducerSlice)).track()
    }

    override fun saveState(state: State, handle: SavedStateHandle) {
        println("State saved")
        handle.set("state", state)
    }

    override fun restoreState(handle: SavedStateHandle): State? {
        val restored = handle.get<State>("state")
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

    private lateinit var mainContent: View
    private lateinit var resultTextView: TextView
    private lateinit var startSampleButton: Button
    private lateinit var progressBar: ProgressBar
    private val viewModel: MainStoreViewModel by viewModels()

    override suspend fun whenCreated(savedInstanceState: Bundle?) {
        setContentView(R.layout.sample_activity)
        mainContent = findViewById(R.id.mainContent)
        resultTextView = findViewById(R.id.resultTextView)
        startSampleButton = findViewById(R.id.startSampleButton)
        progressBar = findViewById(R.id.progressBar)

        startSampleButton.setOnClickListener {
            launch {
                dispatcher.dispatch(LongUseCaseAction("HyperDevs"))
                // Decide on the state after usecase is done
                // I won't run until use case is done
            }
        }

        viewModel.flow()
                .onEach {
                    resultTextView.text = it.toString()
                    progressBar.visibility = if (it.loading) View.VISIBLE else View.INVISIBLE
                    startSampleButton.visibility = if (it.loading) View.INVISIBLE else View.VISIBLE
                }.launchInLifecycleScope()

        viewModel.flow()
                .select { it.loading }
                .onEachDisable {
                    Toast.makeText(this@ViewModelSampleActivity, "Finished loading", Toast.LENGTH_LONG).show()
                }.launchInLifecycleScope()
    }
}
