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

package mini.testing

import mini.BaseAction
import mini.Chain
import mini.Interceptor
import java.util.*

/**
 * [Interceptor] class for testing purposes which mute all the received actions.
 */
internal class TestDispatcherInterceptor : Interceptor {
    override fun invoke(action: Any, chain: Chain): Any {
        println("Muted: $action")
        mutedActions.add(action)
        return TestOnlyAction
    }

    private val mutedActions = LinkedList<Any>()

    val actions: List<Any> get() = mutedActions
}

/**
 * Action for testing purposes.
 */
internal object TestOnlyAction : BaseAction()