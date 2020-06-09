package blogify.common.util

import com.github.kittinunf.result.coroutines.SuspendableResult

import kotlinx.coroutines.runBlocking

import java.lang.IllegalStateException


typealias Sr<V> = SuspendableResult<V, Exception>
typealias SrList<V> = SuspendableResult<List<V>, Exception>

/**
 * Wraps a generic producer function in a [Sr]
 *
 * @author Benjozork
 */
@Suppress("FunctionName")
suspend fun <T : Any> Wrap(producer: suspend () -> T): Sr<T> =
    SuspendableResult.of(producer)

/**
 * Wraps a generic producer function in a [Sr] without suspending
 *
 * @author Benjozork
 */
@Suppress("FunctionName")
fun <T : Any> WrapBlocking(producer: suspend () -> T): Sr<T> =
    runBlocking {
        SuspendableResult.of<T, Exception>(producer)
    }

/**
 * Converts a [SuspendableResult] into a boolean value, using the obvious mapping between the two
 *
 * @author Benjozork
 */
fun <V : Any, E : Exception> SuspendableResult<V, E>.asBoolean(): Boolean {
    return when (this) {
        is SuspendableResult.Success -> true
        is SuspendableResult.Failure -> false
    }
}

/**
 * Converts a boolean value into a [SuspendableResult], using the obvious mapping between the two
 *
 * @author Benjozork
 */
fun Boolean.asResult(): Sr<Unit> {
    return if (this)
        SuspendableResult.Success(Unit)
    else
        SuspendableResult.Failure(IllegalStateException())
}

/**
 * Fetches the value of a [Result] or returns the result of [block] with the error if the result is a [Result.Failure]
 *
 * @author Benjozork
 */
suspend fun <V : Any, E : Exception> SuspendableResult<V, E>.getOr(block: suspend (E) -> V): V =
    when (this) {
        is SuspendableResult.Success -> get()
        is SuspendableResult.Failure -> block(this.error)
    }

/**
 * Fetches the value of a [Result] or evaluates [never] if the result is a [Result.Failure]
 *
 * @author Benjozork
 */
fun <V : Any, E : Exception> SuspendableResult<V, E>.assertGet(): V =
    if (this is SuspendableResult.Success) get() else blogify.common.util.never
