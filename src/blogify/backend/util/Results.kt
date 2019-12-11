package blogify.backend.util

import blogify.backend.routes.pipelines.pipelineError

import io.ktor.http.HttpStatusCode

import com.github.kittinunf.result.coroutines.SuspendableResult

open class BException(causedBy: Exception) : Exception(causedBy)

typealias Sr<V> = SuspendableResult<V, Exception>
typealias SrList<V> = SuspendableResult<List<V>, Exception>

suspend fun <T : Any> wrap(producer: suspend () -> T): Sr<T> = Sr.of(producer)

fun <V : Any, E : Exception> SuspendableResult<V, E>.getOrPipelineError (
    code:    HttpStatusCode = HttpStatusCode.InternalServerError,
    message: String = "error while fetching generic result"
): V {
    when (this) {
        is SuspendableResult.Success -> return this.get()
        is SuspendableResult.Failure -> pipelineError(code, message)
    }
}