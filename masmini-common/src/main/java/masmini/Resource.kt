@file:Suppress("UNCHECKED_CAST")

package masmini

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
    val isIdle: Boolean get() = isEmpty

    internal class Empty {
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

    @Throws(NullPointerException::class)
    fun get() : T = getOrNull()!!

    companion object {
        fun <T> success(value: T? = null): Resource<T> = Resource(value)
        fun <T> failure(exception: Throwable? = null): Resource<T> = Resource(Failure(exception))
        fun <T> loading(value: T? = null): Resource<T> = Resource(Loading(value))
        fun <T> empty(): Resource<T> = Resource(Empty())
        fun <T> idle(): Resource<T> = empty()
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
 * An alias for a empty resource.
 */
typealias Task = Resource<Nothing?>

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

inline fun <T> Resource<T>.onEmpty(crossinline action: () -> Unit): Resource<T> {
    if (isEmpty) action()
    return this
}

/** Alias of [onEmpty] for Task */
inline fun Task.onIdle(crossinline action: () -> Unit) = onEmpty(action)

inline fun <T, R> Resource<T>.map(crossinline transform: (data: T) -> R): Resource<R> {
    if (isSuccess) return Resource.success(transform(value as T))
    return Resource(value)
}

/** All tasks succeeded. */
fun <T> Iterable<Resource<T>>.allSuccessful(): Boolean {
    return this.all { it.isSuccess }
}

/** Any tasks failed. */
fun <T> Iterable<Resource<T>>.anyFailure(): Boolean {
    return this.any { it.isFailure }
}

/** Any task is running. */
fun <T> Iterable<Resource<T>>.anyLoading(): Boolean {
    return this.any { it.isLoading }
}


/** All resources completed, whether they're in success or failure state. */
fun <T> Iterable<Resource<T>>.allTerminal(): Boolean {
    return this.all { it.isTerminal }
}

/** Any resource empty */
fun <T> Iterable<Resource<T>>.anyEmpty(): Boolean = this.any { it.isEmpty }

fun <T> Iterable<Resource<T>>.onAllTerminal(fn: () -> Unit): Iterable<Resource<T>> {
    if (this.allTerminal()) fn()
    return this
}

fun <T> Iterable<Resource<T>>.onAllSuccessful(fn: () -> Unit): Iterable<Resource<T>> {
    if (this.allSuccessful()) fn()
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

/** Returns the first exception that can be found in a list of resources, null if it can't find any */
fun <T> Iterable<Resource<T>>.firstExceptionOrNull() : Throwable? =
    this.firstOrNull { it.isFailure && it.exceptionOrNull() != null }?.exceptionOrNull()

