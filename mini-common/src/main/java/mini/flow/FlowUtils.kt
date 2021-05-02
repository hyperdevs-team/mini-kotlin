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

package mini.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mini.Store

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged].
 */
@FlowPreview
fun <T, R> Flow<T>.select(mapper: suspend (T) -> R): Flow<R> {
    return this
        .map { mapper(it) }
        .distinctUntilChanged()
}

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged] ignoring null values.
 */
@FlowPreview
fun <T, R : Any> Flow<T>.selectNotNull(mapper: suspend (T) -> R?): Flow<R> {
    return this
        .map { mapper(it) }
        .filterNotNull()
        .distinctUntilChanged()
}

/**
 * Return the channel that will emit state changes.
 *
 * @param hotStart emit current state when starting.
 */
@FlowPreview
fun <S : Any> Store<S>.channel(hotStart: Boolean = true, capacity: Int = Channel.BUFFERED): Channel<S> {
    val channel = Channel<S>(capacity)
    val subscription = subscribe(hotStart) {
        channel.offer(it)
    }
    channel.invokeOnClose {
        subscription.close()
    }
    return channel
}

/**
 * Return the flow that will emit state changes.
 *
 * @param hotStart emit current state when starting.
 */
@FlowPreview
fun <S : Any> Store<S>.flow(hotStart: Boolean = true, capacity: Int = Channel.BUFFERED): Flow<S> {
    return channel(hotStart = hotStart, capacity = capacity).consumeAsFlow()
}
