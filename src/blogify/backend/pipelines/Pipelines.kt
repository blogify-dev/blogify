package blogify.backend.pipelines

import blogify.common.util.toUUIDOrNull
import blogify.backend.annotations.PipelinesDsl
import blogify.backend.appContext
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.entity.Resource
import blogify.backend.util.getOr404OrPipelineError
import blogify.backend.util.reason

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

import com.andreapivetta.kolor.red

import org.slf4j.LoggerFactory

import java.util.UUID

private val logger = LoggerFactory.getLogger("blogify-pipeline-manager")

/**
 * Represents a server call pipeline before being wrapped into a [RequestContext]
 *
 * Note: this should not be used as the receiver for any processing pipeline. Please see [RequestContext]
 * as that should be used as receiver when creating a pipeline function.
 *
 * @author Benjozork
 */
typealias GenericCallPipeline = PipelineContext<*, ApplicationCall>

/**
 * Represents an error that occurs while a request pipeline is being executed. Interrupts the pipeline and responds with the given [status code][code] and
 * [error message][message]
 *
 * @property code    the [HttpStatusCode] to respond to the originating request with
 * @property message the message body, which will be wrapped in [reason]
 *
 * @author Benjozork
 */
class PipelineException(val code: HttpStatusCode, override val message: String) : Exception(message)

/**
 * Initially handles and wraps a request by creating a [RequestContext] from an [ApplicationContext].
 *
 * If any [PipelineException] occurs during the execution of [function] and it is not handled inside that same function,
 * an error is sent to the client.
 *
 * @receiver a [PipelineContext] with an [ApplicationContext] as subject and [ApplicationCall] as context
 *
 * @param function              the function to be run inside the request context
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
suspend fun GenericCallPipeline.requestContext (
    function: RequestContextFunction<Unit>
) {
    try {
        RequestContext(appContext, this, call)
            .execute(function, Unit)
    }  catch (e: PipelineException) {
        call.respond(e.code, reason(e.message))
    } catch (e: Exception) {
        logger.error (
            """
                |unhandled exception in pipeline - ${e::class.simpleName} - ${e.message}
                |${e.stackTrace.joinToString(prefix = "\t", separator = "\n\t")}
                """.trimMargin()
        )
        call.respond(HttpStatusCode.InternalServerError, reason("unhandled exception in pipeline"))
    }
}

/**
 * Returns a query parameter that must exist
 */
@PipelinesDsl
@ExperimentalStdlibApi
fun RequestContext.param(name: String) = call.parameters[name] ?: pipelineError(message = "query parameter $name is null")

/**
 * Returns a query parameter that may or may not exist
 */
@PipelinesDsl
@ExperimentalStdlibApi
fun RequestContext.optionalParam(name: String): String? =
    call.parameters[name]

/**
 * Returns a UUID from a `{uuid}` query parameter. Responds with `Bad Request` if it is not found or invalid.
 */
@PipelinesDsl
@ExperimentalStdlibApi
inline val RequestContext.queryUuid get() = object : ReadOnlyProperty<Nothing?, UUID> {
    override fun getValue(thisRef: Nothing?, property: KProperty<*>) =
        param("uuid").toUUIDOrNull()
            ?: pipelineError(HttpStatusCode.BadRequest, "mandatory 'uuid' parameter not found or in the wrong format")
}

/**
 * Simplifies fetching a resource from a [RequestContext]
 */
@PipelinesDsl
@ExperimentalStdlibApi
suspend inline fun <reified R : Resource> RequestContext.obtainResource(id: UUID): R {
    return repository<R>().obtain(id)
        .getOr404OrPipelineError(HttpStatusCode.InternalServerError, "couldn't fetch resource")
}

/**
 * Simplifies fetching resources from a [RequestContext]
 */
@PipelinesDsl
@ExperimentalStdlibApi
suspend inline fun <reified R : Resource> RequestContext.obtainResources(limit: Int = 25): List<R> {
    return repository<R>().obtainAll(limit)
        .getOr404OrPipelineError(HttpStatusCode.InternalServerError, "couldn't fetch resource")
}

/**
 * Signals that a [RequestContext] has encountered an error, and will stop being executed.
 * This function throws a [PipelineException], and therefore stops the entire pipeline call chain, entering its own request handler.
 *
 * @param code          the [HttpStatusCode] to respond to the originating request with. Defaults to 400 Bad Request.
 * @param message       the message body, which will be wrapped in [reason]
 * @param rootException the exception, if it exists, that caused this error
 *
 * @throws PipelineException to signal the end of the pipeline chain and trigger the pipeline error request handler
 *
 * @author Benjozork
 */
fun pipelineError(code: HttpStatusCode = HttpStatusCode.BadRequest, message: String, rootException: Exception? = null): Nothing {
    logger.debug (
        "pipeline error - $message".red()
                + (rootException?.let { " - ${it::class.simpleName} - ${it.message}".red() } ?: "")
    )
    rootException?.printStackTrace()
    throw PipelineException(code, message)
}

/**
 *  This calls [pipelineError] with [HttpStatusCode.InternalServerError] as `code`,
 *  message of [exception] as `message` and [exception] as `rootException`
 *
 *  @param exception The exception that occurred
 *
 *  @author hamza1311
 */
fun pipelineError(exception: Exception) {
    pipelineError(
        code = HttpStatusCode.InternalServerError,
        message = exception.message ?: "An error occurred",
        rootException = exception
    )
}
