/*
 * Copyright 2026 HyperDevs
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

import android.util.Log
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mini.android.sample.counter.CounterState
import mini.android.sample.counter.CounterFeatureRuntime
import mini.android.sample.ui.theme.AppTheme

class CounterFeatureSampleActivity : AppCompatActivity() {

    private val counterFeature = CounterFeatureRuntime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MiniSample", "Counter feature runs with its own local Mini runtime inside the main sample app")

        setContent {
            AppTheme {
                CounterFeatureSampleScreen()
            }
        }
    }

    override fun onDestroy() {
        counterFeature.close()
        super.onDestroy()
    }

    @Composable
    private fun CounterFeatureSampleScreen() {
        val coroutineScope = rememberCoroutineScope()
        val counterState by counterFeature.flow().collectAsState(initial = CounterState())

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Counter feature state: ${counterState.count}")
                Button(onClick = {
                    counterFeature.increment(coroutineScope)
                }) {
                    Text("Run counter feature")
                }
            }
        }
    }
}
