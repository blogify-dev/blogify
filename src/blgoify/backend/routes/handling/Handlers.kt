package blgoify.backend.routes.handling

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

import blgoify.backend.resources.models.Resource
import blgoify.backend.util.toUUID

import java.util.UUID

/**
 * Represents a server call pipeline
 */
typealias CallPipeline = PipelineContext<Unit, ApplicationCall>

/**
 * Adds a handler to a [CallPipeline] that handles fetching a resource.
 *
 * @param R         the type of [Resource] to be fetched
 * @param fetch     the [function][Function] that retrieves the resource
 * @param transform a transformation [function][Function] that transforms the [resource][Resource] before sending it back to the client
 */
suspend fun <R : Resource> CallPipeline.handleResourceFetch (
    fetch:     suspend (id: UUID)   -> R?,
    transform: suspend (fetched: R) -> Any = { it }
) {
    call.parameters["uuid"]?.let { id ->
        fetch.invoke(id.toUUID())?.let { resource ->
            call.respond(transform.invoke(resource))
        } ?: call.respond(HttpStatusCode.NotFound)
    } ?: call.respond(HttpStatusCode.BadRequest)
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources.
 *
 * @param R         the type of [Resource] to be fetched
 * @param fetch     the [function][Function] that retrieves the resources
 * @param transform a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client
 */
suspend fun <R : Resource> CallPipeline.handleResourceFetchAll (
    fetch:     suspend ()        -> Collection<R>,
    transform: suspend (elem: R) -> Any = { it }
) {
    fetch.invoke().takeIf { it.isNotEmpty() }?.let { set ->
        call.respond(set.map { transform.invoke(it) })
    } ?: call.respond(HttpStatusCode.NoContent)
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources that are related to a particular resource.
 *
 * @param R         the type of [Resource] to be fetched
 * @param fetch     the [function][Function] that retrieves the resources using the [ID][UUID] of another resource
 * @param transform a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client
 */
suspend fun <R : Resource> CallPipeline.handleIdentifiedResourceFetchAll (
    fetch:     suspend (id: UUID) -> Collection<R>,
    transform: suspend (elem: R)  -> Any = { it }
) {
    call.parameters["uuid"]?.let { id ->
        fetch.invoke(id.toUUID()).takeIf { it.isNotEmpty() }?.let { set ->
            call.respond(set.map { transform.invoke(it) })
        } ?: call.respond(HttpStatusCode.NoContent)
    } ?: call.respond(HttpStatusCode.NotFound)
}

/* @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend inline fun <reified R : Resource> CallPipeline.handleSimpleResourceCreation (
    creationFunction: suspend (res: R) -> Boolean
) {
    try {
        val rec = call.receive<R>()
        val res = creationFunction.invoke(rec)
        if (res) {
            call.respond(HttpStatusCode.Created)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    } catch (e: ContentTransformationException) {
        call.respond(HttpStatusCode.BadRequest)
    }
} // KT-33440 | Doesn't compile when called for now */