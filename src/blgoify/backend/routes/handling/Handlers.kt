package blgoify.backend.routes.handling

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive

import blgoify.backend.resources.models.Resource
import blgoify.backend.services.models.ResourceResult
import blgoify.backend.services.models.ResourceResultSet
import blgoify.backend.util.BlogifyDsl
import blgoify.backend.util.toUUID

import java.util.UUID

/**
 * Represents a server call pipeline
 */
typealias CallPipeline = PipelineContext<Unit, ApplicationCall>

/**
 * Represents a server call handler function.
 */
typealias CallPipeLineFunction = PipelineInterceptor<Unit, ApplicationCall>

/**
 * Adds a handler to a [CallPipeline] that handles fetching a resource.
 *
 * @param R         the type of [Resource] to be fetched
 * @param fetch     the [function][Function] that retrieves the resource
 * @param transform a transformation [function][Function] that transforms the [resource][Resource] before sending it back to the client
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.handleResourceFetch (
    fetch:     suspend (id: UUID)   -> ResourceResult<R>,
    transform: suspend (fetched: R) -> Any = { it }
) {
    call.parameters["uuid"]?.let { id -> // Check if the query URL provides any UUID
        fetch.invoke(id.toUUID()).fold ( // Yes ? Proceed.
            success = {
                call.respond(transform.invoke(it)) // Success ? Then respond with the transform function's result.
            },
            failure = {
                call.respond(object { val message = it.message }) // Failure ? Error code for now.
            }
        )
    } ?: call.respond(HttpStatusCode.BadRequest) // No ? Bad Request.
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources.
 *
 * @param R         the type of [Resource] to be fetched
 * @param fetch     the [function][Function] that retrieves the resources
 * @param transform a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.handleResourceFetchAll (
    fetch:     suspend ()        -> ResourceResultSet<R>,
    transform: suspend (elem: R) -> Any = { it }
) {
    fetch.invoke().fold ( // Start by calling the fetching function
        success = { set -> // Success ? Proceed.
            set.takeIf { set.isNotEmpty() }?.let { notEmptySet -> // Is the set empty ?
                call.respond(notEmptySet.map { transform.invoke(it) }) // No ? Then respond with the transform function's result.
            } ?: call.respond(HttpStatusCode.NoContent) // Yes ? No Content.
        },
        failure = {
            call.respond(object { val message = it.message }) // Failure ? Error code for now.
        }
    )

}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources that are related to a particular resource.
 *
 * @param R         the type of [Resource] to be fetched
 * @param fetch     the [function][Function] that retrieves the resources using the [ID][UUID] of another resource
 * @param transform a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.handleIdentifiedResourceFetchAll (
    fetch:     suspend (id: UUID) -> ResourceResultSet<R>,
    transform: suspend (elem: R)  -> Any = { it }
) {
    call.parameters["uuid"]?.let { id -> // Check if the query URL provides any UUID
        fetch.invoke(id.toUUID()).fold ( // Yes ? Proceed and call the fetching funciton.
            success = { set -> // Success ? Proceed.
                set.takeIf { it.isNotEmpty() }?.let { notEmptySet -> // Is the set empty ?
                    call.respond(notEmptySet.map { transform.invoke(it)}) // No ? Then respond with the transform function's result.
                } ?: call.respond(HttpStatusCode.NoContent) // Yes ? No Content.
            },
            failure = { // Failure ? Error code for now.
                call.respond(object { val message = it.message })
            }
        )
    } ?: call.respond(HttpStatusCode.BadRequest) // No ? Bad Request.
}

// TODO result-ify this
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> CallPipeline.handleResourceCreation (
    creationFunction: suspend (res: R) -> Boolean
) {
    try {
        val rec = call.receive<R>()
        val res = creationFunction(rec)
        if (res) {
            call.respond(HttpStatusCode.Created)
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    } catch (e: ContentTransformationException) {
        call.respond(HttpStatusCode.BadRequest)
    }
} // KT-33440 | Doesn't compile when lambda called with invoke() for now */

suspend fun CallPipeline.handleResourceDeletion (
    deletionFunction: suspend (id: UUID) -> Boolean
) {
    call.parameters["uuid"]?.let { id ->
        deletionFunction.invoke(id.toUUID()).takeIf { it }?.let {
            call.respond(HttpStatusCode.OK)
        } ?: call.respond(HttpStatusCode.InternalServerError)
    } ?: call.respond(HttpStatusCode.BadRequest)
}