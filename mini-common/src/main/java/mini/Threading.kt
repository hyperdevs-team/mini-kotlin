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

package mini

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Semaphore

val uiHandler by lazy {
    requireAndroid()
    Handler(Looper.getMainLooper())
}

fun assertOnUiThread() {
    if (!isAndroid) return
    if (Looper.myLooper() != Looper.getMainLooper()) {
        error("This method can only be called from the main application thread")
    }
}

fun assertOnBgThread() {
    if (!isAndroid) return
    if (Looper.myLooper() == Looper.getMainLooper()) {
        error("This method can only be called from non UI threads")
    }
}

@JvmOverloads
inline fun onUi(delayMs: Long = 0, crossinline block: () -> Unit) {
    requireAndroid()
    if (delayMs > 0) uiHandler.postDelayed({ block() }, delayMs)
    else uiHandler.post { block() }
}

inline fun <T> onUiSync(crossinline block: () -> T) {
    uiHandler.postSync(block)
}

inline fun <T> Handler.postSync(crossinline block: () -> T) {
    requireAndroid()
    if (Looper.myLooper() == this.looper) {
        block()
    } else {
        val sem = Semaphore(0)
        post {
            block()
            sem.release()
        }
        sem.acquireUninterruptibly()
    }
}