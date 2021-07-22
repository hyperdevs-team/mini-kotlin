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

import mini.Action
import mini.Reducer
import mini.SuspendingAction
import java.io.Serializable

data class State(
        val text: String = "0",
        val loading: Boolean = false
) : Serializable

@Action
data class SetLoadingAction(val loading: Boolean)

@Action
data class SetTextAction(val text: String)

@Action
interface AnalyticsAction

@Action
class LongUseCaseAction(val userName: String) : AnalyticsAction, SuspendingAction

/**
 * Use any name you like for suspending actions, or use reducer
 */
typealias UseCase = Reducer