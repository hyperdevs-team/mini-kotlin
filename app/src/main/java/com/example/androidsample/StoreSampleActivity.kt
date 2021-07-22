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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mini.*
import mini.android.FluxActivity

private val dispatcher = Dispatcher()

class MainStore : Store<State>() {

    init {
        Mini.link(dispatcher, this).track()
    }

    @Reducer
    fun handleLoading(state: State, action: SetLoadingAction): State {
        return state.copy(loading = action.loading)
    }

    @Reducer
    fun handleSetTextAction(state: State, action: SetTextAction): State {
        return state.copy(text = action.text)
    }

    @Reducer
    fun handleAnalyticsAction(action: AnalyticsAction) {
        //Log to analytics
    }

    @Reducer
    fun handleAnyAction(action: Any) {
        //Log to analytics
    }

    @UseCase
    suspend fun useCase(action: LongUseCaseAction) {
        if (state.loading) return
        dispatcher.dispatch(SetLoadingAction(true))
        dispatcher.dispatch(SetTextAction("Loading from network..."))
        delay(5000)
        dispatcher.dispatch(SetTextAction("Hello From UseCase"))
        dispatcher.dispatch(SetLoadingAction(false))
    }
}

class StoreSampleActivity : FluxActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var startSampleButton: Button
    private lateinit var progressBar: ProgressBar

    private val mainStore: MainStore by lazy {
        MainStore()
    }

    override suspend fun whenCreated(savedInstanceState: Bundle?) {
        setContentView(R.layout.sample_activity)
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

        mainStore.flow()
                .onEach {
                    resultTextView.text = it.toString()
                    progressBar.visibility = if (it.loading) View.VISIBLE else View.INVISIBLE
                    startSampleButton.visibility = if (it.loading) View.INVISIBLE else View.VISIBLE
                }.launchInLifecycleScope()

        mainStore.flow()
                .select { it.loading }
                .onEachDisable {
                    Toast.makeText(this@StoreSampleActivity, "Finished loading", Toast.LENGTH_LONG).show()
                }.launchInLifecycleScope()
    }
}
