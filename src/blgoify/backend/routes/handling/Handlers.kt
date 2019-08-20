package blgoify.backend.routes.handling

import blgoify.backend.resources.models.Resource
import blgoify.backend.util.toUUID

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

import java.util.UUID

suspend fun <R : Resource> PipelineContext<Unit, ApplicationCall>.handleSimpleResourceFetch (
    retrieveFunction:  suspend (id: UUID) -> R?,
    transformFunction: suspend (R)        -> Any = { it }
) {
    call.parameters["uuid"]?.let { id ->
        retrieveFunction.invoke(id.toUUID())?.let { resource ->
            call.respond(transformFunction.invoke(resource))
        } ?: call.respond(HttpStatusCode.NotFound)
    } ?: call.respond(HttpStatusCode.BadRequest)
}

@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend inline fun <reified R : Resource> PipelineContext<Unit, ApplicationCall>.handleSimpleResourceCreation (
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
}