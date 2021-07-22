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

/**
 * Middleware that will be called for every dispatch to modify the
 * action or perform side effects like logging.
 *
 * Call chain.proceed(action) with the new action or dispatcher chain will be broken.
 */
interface Middleware {
    suspend fun intercept(action: Any, chain: Chain): Any
}

/**
 * A chain of interceptors. Call [proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain {
    suspend fun proceed(action: Any): Any
}
