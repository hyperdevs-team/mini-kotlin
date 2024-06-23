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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import mini.android.sample.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            AppTheme {
                MainScreen(
                    onGoToStoreSampleClicked = {
                        Intent(this, StoreSampleActivity::class.java).apply {
                            startActivity(this)
                        }
                    },
                    onGoToViewModelSampleClicked = {
                        Intent(this, ViewModelSampleActivity::class.java).apply {
                            startActivity(this)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainScreen(modifier: Modifier = Modifier,
                       onGoToStoreSampleClicked: () -> Unit = {},
                       onGoToViewModelSampleClicked: () -> Unit = {}) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        MainContent(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            onGoToStoreSampleClicked = onGoToStoreSampleClicked,
            onGoToViewModelSampleClicked = onGoToViewModelSampleClicked
        )
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    onGoToStoreSampleClicked: () -> Unit = {},
    onGoToViewModelSampleClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onGoToStoreSampleClicked) {
            Text("Go to Store sample")
        }
        Button(onClick = onGoToViewModelSampleClicked) {
            Text("Go to ViewModel sample")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainContent()
    }
}
