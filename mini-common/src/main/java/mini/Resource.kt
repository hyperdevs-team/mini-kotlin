@file:Suppress("UNCHECKED_CAST")

package mini

/**
 * Simple wrapper to map ongoing tasks (network / database) for view implementation.
 *
 * Similar to kotlin [Result] but with loading and empty state.
 */
open class Resource<out T> @PublishedApi internal constructor(val value: Any?) {

    val isSuccess: Boolean get() = !isLoading && !isFailure && !isEmpty
    val isEmpty: Boolean get() = value is Empty
    val isFailure: Boolean get() = value is Failure
    val isLoading: Boolean get() = value is Loading<*>

    val isTerminal: Boolean get() = isSuccess || isFailure

    internal object Empty {
        override fun toString(): String = "Empty()"
    }

    @PublishedApi
    internal data class Failure(val exception: Throwable?) {
        override fun toString(): String = "Failure($exception)"
    }

    @PublishedApi
    internal data class Loading<U>(val value: U? = null) {
        override fun toString(): String = "Loading($value)"
    }

    /**
     * Get the current value if successful, or null for other cases.
     */
    fun getOrNull(): T? =
        when {
            isLoading -> (value as Loading<T>).value
            isSuccess -> value as T?
            else -> null
        }

    fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    companion object {
        fun <T> success(value: T): Resource<T> = Resource(value)
        fun <T> failure(exception: Throwable? = null): Resource<T> = Resource(Failure(exception))
        fun <T> loading(value: T? = null): Resource<T> = Resource(Loading(value))
        fun <T> empty(): Resource<T> = Resource(Empty)
    }

    override fun toString(): String {
        return if (isSuccess) "Success($value)" else value.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value?.hashCode() ?: 0
}

/**
 * A resource that abstracts asynchronous operation but with idle
 * state instead of empty.
 *
 * It accepts temporary metadata objects as value.
 */
open class TypedTask<T> @PublishedApi internal constructor(value: Any?) : Resource<T>(value) {
    val isIdle: Boolean get() = isEmpty

    companion object {
        fun <T> success(value: T): TypedTask<T> = TypedTask(value)
        fun <T> idle(): TypedTask<T> = TypedTask(Empty)
        fun <T> loading(value: T? = null): TypedTask<T> = TypedTask(Loading(value))
        fun <T> failure(exception: Throwable? = null): TypedTask<T> = TypedTask(Failure(exception))
    }

    override fun toString(): String {
        return when {
            isSuccess -> "Success"
            isIdle -> "Idle"
            else -> value.toString()
        }
    }
}

/**
 * An empty resource that just abstracts asynchronous operation but with idle
 * state instead of empty.
 */
class Task(value: Any?) : TypedTask<Unit>(value) {
    companion object {
        fun success(): Task = Task(Unit)
        fun idle(): Task = Task(Empty)
        fun loading(): Task = Task(Loading<Unit>())
        fun failure(exception: Throwable? = null): Task = Task(Failure(exception))
    }
}

inline fun <T> Resource<T>.onSuccess(crossinline action: (data: T) -> Unit): Resource<T> {
    if (isSuccess) action(value as T)
    return this
}

inline fun <T> Resource<T>.onFailure(crossinline action: (error: Throwable?) -> Unit): Resource<T> {
    if (isFailure) action((value as Resource.Failure).exception)
    return this
}

inline fun <T> Resource<T>.onLoading(crossinline action: (data: T?) -> Unit): Resource<T> {
    if (isLoading) action((value as Resource.Loading<T>).value)
    return this
}

inline fun <T> TypedTask<T>.onIdle(crossinline action: () -> Unit): TypedTask<T> {
    if (isEmpty) action()
    return this
}

inline fun <T> Resource<T>.onEmpty(crossinline action: () -> Unit): Resource<T> {
    if (isEmpty) action()
    return this
}

inline fun <T, R> Resource<T>.map(crossinline transform: (data: T) -> R): Resource<R> {
    if (isSuccess) return Resource.success(transform(value as T))
    return Resource(value)
}

/** All resources succeeded. */
fun <T> Iterable<Resource<T>>.allSuccesful(): Boolean {
    return this.all { it.isSuccess }
}

/** Any resources failed. */
fun <T> Iterable<Resource<T>>.anyFailure(): Boolean {
    return this.any { it.isFailure }
}

/** Any resource is loading. */
fun <T> Iterable<Resource<T>>.anyLoading(): Boolean {
    return this.any { it.isLoading }
}

/** Any resource empty */
fun <T> Iterable<Resource<T>>.anyEmpty(): Boolean = this.any { it.isEmpty }

fun <T> Iterable<Resource<T>>.onAllSuccessful(fn: () -> Unit): Iterable<Resource<T>> {
    if (this.allSuccesful()) fn()
    return this
}

fun <T> Iterable<Resource<T>>.onAnyFailure(fn: () -> Unit): Iterable<Resource<T>> {
    if (this.anyFailure()) fn()
    return this
}

fun <T> Iterable<Resource<T>>.onAnyLoading(fn: () -> Unit): Iterable<Resource<T>> {
    if (this.anyLoading()) fn()
    return this
}

fun <T> Iterable<Resource<T>>.onAnyEmpty(fn: () -> Unit): Iterable<Resource<T>> {
    if (this.anyEmpty()) fn()
    return this
}

fun Iterable<Task>.onAnyIdle(fn: () -> Unit): Iterable<Task> = onAnyEmpty(fn).map { it as Task }