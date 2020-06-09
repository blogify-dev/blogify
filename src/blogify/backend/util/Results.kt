package blogify.backend.util

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.pipelines.PipelineException
import blogify.backend.pipelines.pipelineError

import io.ktor.http.HttpStatusCode

import com.github.kittinunf.result.coroutines.SuspendableResult

/**
 * Fetches the value of a [Result] or throws a [PipelineException] if the result is a [Result.Failure]
 *
 * @author Benjozork
 */
@BlogifyDsl
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
 * Fetches the value of a [Result]. Responds with 404 if a result is not found.
 * If the result is a [Result.Failure], throws a [PipelineException]
 *
 * @throws PipelineException
 *
 * @author hamza1311
 */
@BlogifyDsl
fun <V : Any, E : Exception> SuspendableResult<V, E>.getOr404OrPipelineError (
    code: HttpStatusCode = HttpStatusCode.InternalServerError,
    message: String = "error while fetching generic result"
): V {
    when (this) {
        is SuspendableResult.Success -> return this.get()
        is SuspendableResult.Failure -> {
            val exception = this.getException()

            if (exception is NoSuchElementException) {
                pipelineError(HttpStatusCode.NotFound, "Requested resource not found")
            }
            else pipelineError(code, message, this.error)
        }
    }
}
