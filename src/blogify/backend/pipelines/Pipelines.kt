package blogify.backend.pipelines

import blogify.backend.annotations.PipelinesDsl
import blogify.backend.applicationContext
import blogify.backend.auth.handling.UserAuthPredicate
import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.routing.handling.defaultResourceLessPredicateLambda
import blogify.backend.routing.handling.logUnusedAuth
import blogify.backend.util.MapCache
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import io.ktor.response.respond

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
 * If any [PipelineException] occurs during the execution of [block] and it is not handled inside that same function,
 * an error is sent to the client.
 *
 * @receiver a [PipelineContext] with an [ApplicationContext] as subject and [ApplicationCall] as context
 *
 * @param applicationContext the application context to use to create the request context
 * @param block              the function to be run inside the request context
 *
 * @author Benjozork
 */
suspend fun GenericCallPipeline.requestContext (
    applicationContext: ApplicationContext,
    block: RequestContextFunction<Unit>
) {
    try {
        RequestContext(applicationContext, this, call)
            .execute(block, Unit)
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
fun RequestContext.param(name: String) = call.parameters[name] ?: pipelineError(message = "query parameter $name is null")

/**
 * Returns a query parameter that may or may not exist
 */
@PipelinesDsl
fun RequestContext.optionalParam(name: String): String? = call.parameters[name]

/**
 * A default [RequestContext] that handles client authentication.
 *
 * @param funcName  the name of the pipeline using this pipeline. Only for logging purposes.
 * @param predicate the [UserAuthPredicate] to run as authentication
 * @param block     the actual pipeline code
 *
 * @author Benjozork
 */
@PipelinesDsl
suspend fun RequestContext.handleAuthentication (
    funcName:  String = "<unspecified>",
    predicate: UserAuthPredicate,
    block:     RequestContextFunction<User?>
) {
    if (predicate != defaultResourceLessPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
        runAuthenticated(predicate, { subject -> block(this@handleAuthentication, subject) })
    } else {
        logUnusedAuth(funcName)
        block(this, null)
    }
}

/**
 * Simplifies fetching a resource from a [RequestContext]
 */
@PipelinesDsl
suspend inline fun <reified R : Resource> RequestContext.obtainResource(id: UUID): R {
    return (repository<R>()::get)(this, id)
        .getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't fetch resource")
}

/**
 * Simplifies fetching resources from a [RequestContext]
 */
@PipelinesDsl
suspend inline fun <reified R : Resource> RequestContext.obtainResources(limit: Int = 25): List<R> {
    return (repository<R>()::getAll)(this, limit)
        .getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't fetch resource")
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
