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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import mini.Dispatcher
import mini.LoggerMiddleware
import mini.Mini
import mini.flow
import mini.android.sample.counter.CounterState
import mini.android.sample.counter.CounterStore
import mini.android.sample.counter.IncrementCounterAction
import mini.android.sample.message.AdvanceMessageAction
import mini.android.sample.message.MessageState
import mini.android.sample.message.MessageStore
import mini.android.sample.message.SetMessageAction
import mini.android.sample.ui.theme.AppTheme
import timber.log.Timber
import java.io.Closeable

class MultiRegistrySampleActivity : AppCompatActivity() {

    private val dispatcher = Dispatcher()
    private val counterStore = CounterStore()
    private val messageStore = MessageStore()
    private lateinit var storeSubscriptions: Closeable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storeSubscriptions = Mini.link(dispatcher, listOf(counterStore, messageStore))
        dispatcher.addMiddleware(
            LoggerMiddleware(listOf(counterStore, messageStore), logger = { _, tag, msg ->
                Timber.tag(tag).d(msg)
            })
        )
        counterStore.initialize()
        messageStore.initialize()

        setContent {
            AppTheme {
                MultiRegistrySampleScreen()
            }
        }
    }

    override fun onDestroy() {
        if (this::storeSubscriptions.isInitialized) {
            storeSubscriptions.close()
        }
        counterStore.close()
        messageStore.close()
        super.onDestroy()
    }

    @Composable
    private fun MultiRegistrySampleScreen() {
        val coroutineScope = rememberCoroutineScope()
        val counterState by counterStore.flow().collectAsState(initial = CounterState())
        val messageState by messageStore.flow().collectAsState(initial = MessageState())

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
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    dispatcher.dispatchOn(IncrementCounterAction(), coroutineScope)
                }) {
                    Text("Dispatch action to counter module")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Message feature state: ${messageState.text}")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    dispatcher.dispatchOn(AdvanceMessageAction(), coroutineScope)
                }) {
                    Text("Dispatch action to message module")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    dispatcher.dispatchOn(SetMessageAction("custom-message"), coroutineScope)
                }) {
                    Text("Dispatch custom message action")
                }
            }
        }
    }
}
