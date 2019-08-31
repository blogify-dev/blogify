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
 * Requires a [UUID] to be passed in the query URL.
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
        fetch.invoke(id.toUUID()).fold (
            success = {
                call.respond(transform.invoke(it))
            },
            failure = {
                call.respond(HttpStatusCode.InternalServerError, object { val message = it.message }) // Failure ? Send a simple object with the exception message.
            }
        )
    } ?: call.respond(HttpStatusCode.BadRequest) // If not, send Bad Request.
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
    fetch.invoke().fold (
        success = { set ->
            if (set.isNotEmpty()) {
                call.respond(set.map { transform.invoke(it) })
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        },
        failure = {
            call.respond(HttpStatusCode.InternalServerError, object { val message = it.message }) // Failure ? Send a simple object with the exception message.
        }
    )

}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources that are related to a particular resource.
 *
 * Requires a [UUID] to be passed in the query URL.
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
        fetch.invoke(id.toUUID()).fold (
            success = { set ->
                if (set.isNotEmpty()) {
                    call.respond(set.map { transform.invoke(it) })
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            },
            failure = {
                call.respond(HttpStatusCode.InternalServerError, object { val message = it.message }) // Failure ? Send a simple object with the exception message.
            }
        )
    } ?: call.respond(HttpStatusCode.BadRequest) // If not, send Bad Request.
}

/**
 * Adds a handler to a [CallPipeline] that handles creating a new resource.
 *
 * @param R      the type of [Resource] to be created
 * @param create the [function][Function] that retrieves that creates the resource using the call
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> CallPipeline.handleResourceCreation (
    create: suspend (res: R) -> ResourceResult<R>
) {
    try {
        val rec = call.receive<R>()
        val res = create(rec)

        res.fold (
            success = {
                call.respond(HttpStatusCode.Created)
            },
            failure = {
                call.respond(HttpStatusCode.InternalServerError, object { val message = it.message }) // Failure ? Send a simple object with the exception message.
            }
        )
    } catch (e: ContentTransformationException) {
        call.respond(HttpStatusCode.BadRequest)
    }
} // KT-33440 | Doesn't compile when lambda called with invoke() for now */

/**
 * Adds a handler to a [CallPipeline] that handles deleting a new resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param delete the [function][Function] that retrieves that deletes the specified resource
 */
@BlogifyDsl
suspend fun CallPipeline.handleResourceDeletion (
    delete: suspend (id: UUID) -> ResourceResult<*>
) {
    call.parameters["uuid"]?.let { id ->
        delete.invoke(id.toUUID()).fold (
            success = {
                call.respond(HttpStatusCode.OK)
            },
            failure = {
                call.respond(HttpStatusCode.InternalServerError, object { val message = it.message }) // Failure ? Send a simple object with the exception message.
            }
        )
    } ?: call.respond(HttpStatusCode.BadRequest)
}