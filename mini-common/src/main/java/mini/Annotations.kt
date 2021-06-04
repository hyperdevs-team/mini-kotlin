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

import java.lang.annotation.Inherited

const val DEFAULT_PRIORITY = 100

/**
 * Mark a type as action for code generation. All actions must include this annotation
 * or dispatcher won't work properly.
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Action

/**
 * Mark a function declared in a [StateContainer] as a reducer function.
 *
 * Reducers function must have two parameters, the state that must have same time
 * as the [StateContainer] state, and the action being handled.
 *
 * If the reducer function is not pure, only the action parameter is allowed
 * and function should have no return.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reducer(val priority: Int = DEFAULT_PRIORITY)

