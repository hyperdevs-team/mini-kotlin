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

package mini.rx

import io.reactivex.BackpressureStrategy
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import mini.Store

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Flowable<T>.mapNotNull(crossinline fn: (T) -> U?): Flowable<U> {
    return filter { fn(it) != null }.map { fn(it) }
}

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Observable<T>.mapNotNull(crossinline fn: (T) -> U?): Observable<U> {
    return filter { fn(it) != null }.map { fn(it) }
}

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Flowable<T>.select(crossinline fn: (T) -> U?): Flowable<U> {
    return mapNotNull(fn).distinctUntilChanged()
}

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Observable<T>.select(crossinline fn: (T) -> U?): Observable<U> {
    return mapNotNull(fn).distinctUntilChanged()
}

interface SubscriptionTracker {
    /**
     * Clear Subscriptions.
     */
    fun clearSubscriptions()

    /**
     * Start tracking a disposable.
     */
    fun <T : Disposable> T.track(): T
}

class DefaultSubscriptionTracker : SubscriptionTracker {
    private val disposables = CompositeDisposable()
    override fun clearSubscriptions() = disposables.clear()
    override fun <T : Disposable> T.track(): T {
        disposables.add(this)
        return this
    }
}

fun <S> Store<S>.observable(hotStart: Boolean = true): Observable<S> {
    val subject = PublishSubject.create<S>()
    val subscription = subscribe(hotStart = false) {
        subject.onNext(it)
    }
    return subject
        .doOnDispose { subscription.close() }
        .doOnTerminate { subscription.close() }
        .let { if (hotStart) it.startWith(state) else it }
}

fun <S> Store<S>.flowable(hotStart: Boolean = true,
                          backpressureStrategy: BackpressureStrategy = BUFFER): Flowable<S> =
    observable(hotStart).toFlowable(backpressureStrategy)