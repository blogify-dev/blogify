package blogify.backend.util

import blogify.backend.pipelines.PipelineException
import blogify.backend.pipelines.pipelineError

import io.ktor.http.HttpStatusCode

import com.github.kittinunf.result.coroutines.SuspendableResult

import kotlinx.coroutines.runBlocking

open class BException(causedBy: Exception) : Exception(causedBy)

typealias Sr<V> = SuspendableResult<V, Exception>
typealias SrList<V> = SuspendableResult<List<V>, Exception>

/**
 * Wraps a generic producer function in a [Sr]
 *
 * @author Benjozork
 */
@Suppress("FunctionName")
suspend fun <T : Any> Wrap(producer: suspend () -> T): Sr<T> = Sr.of(producer)

/**
 * Wraps a generic producer function in a [Sr] without suspending
 *
 * @author Benjozork
 */
@Suppress("FunctionName")
fun <T : Any> WrapBlocking(producer: suspend () -> T): Sr<T> = runBlocking { Sr.of<T, Exception>(producer) }

/**
 * Fetches the value of a [Result] or throws a [PipelineException] if the result is a [Result.Failure]
 *
 * @author Benjozork
 */
fun <V : Any, E : Exception> SuspendableResult<V, E>.getOrPipelineError (
    code:    HttpStatusCode = HttpStatusCode.InternalServerError,
    message: String = "error while fetching generic result"
): V {
    when (this) {
        is SuspendableResult.Success -> return this.get()
        is SuspendableResult.Failure -> pipelineError(code, message, this.error)
    }
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
 * Fetches the value of a [Result] or returns `null` if the result is a [Result.Failure]
 *
 * @author Benjozork
 */
fun <V : Any, E : Exception> SuspendableResult<V, E>.getOrNull(): V? =
    if (this is SuspendableResult.Success) get() else null

/**
 * Fetches the value of a [Result] or evaluates [never] if the result is a [Result.Failure]
 *
 * @author Benjozork
 */
fun <V : Any, E : Exception> SuspendableResult<V, E>.assertGet(): V =
    if (this is SuspendableResult.Success) get() else never
