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

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/** Actions implementing this interface won't log anything, including nested calls */
interface SilentAction

/**
 * Actions implementing this interface will log nested actions visually since they will
 * most likely dispatch other actions.
 */
interface SuspendingAction

internal fun extractClassName(clazz: Class<*>): String {
    return clazz.name.substringAfterLast(".")
}

/**
 * Action logging for stores.
 */
class LoggerMiddleware(stores: Collection<StateContainer<*>>,
                       private val tag: String = "MiniLog",
                       private val diffFunction: ((a: Any?, b: Any?) -> String)? = null,
                       private val logger: (priority: Int, tag: String, msg: String) -> Unit) : Middleware {

    private var actionCounter = AtomicInteger(0)

    private val stores = stores.toList()

    override suspend fun intercept(action: Any, chain: Chain): Any {
        if (action is SilentAction) return chain.proceed(action) //Do nothing

        val isSuspending = action is SuspendingAction
        val beforeStates: Array<Any?> = Array(stores.size) { }
        val afterStates: Array<Any?> = Array(stores.size) { }
        val actionName = extractClassName(action.javaClass)

        if (!isSuspending) {
            stores.forEachIndexed { idx, store -> beforeStates[idx] = store.state }
        }

        val (upCorner, downCorner) = if (isSuspending) {
            "╔═════ " to "╚════> "
        } else {
            "┌── " to "└─> "
        }

        val prelude = "[${"${actionCounter.getAndIncrement() % 100}".padStart(2, '0')}] "

        logger(Log.DEBUG, tag, "$prelude$upCorner$actionName")
        logger(Log.DEBUG, tag, "$prelude$action")

        //Pass it down
        val start = System.nanoTime()
        val outAction = chain.proceed(action)
        val processTime = (System.nanoTime() - start) / 1000000

        if (!isSuspending) {
            stores.forEachIndexed { idx, store -> afterStates[idx] = store.state }

            for (i in beforeStates.indices) {
                val oldState = beforeStates[i]
                val newState = afterStates[i]
                if (oldState !== newState) {
                    val line = "$prelude│ ${stores[i].javaClass.name}"
                    logger(Log.VERBOSE, tag, "$line: $newState")
                    diffFunction?.invoke(oldState, newState)?.let { diff ->
                        logger(Log.DEBUG, tag, "$line: $diff")
                    }
                }
            }
        }

        logger(Log.DEBUG, tag, "$prelude$downCorner$actionName ${processTime}ms")

        return outAction
    }
}