@file:Suppress("DuplicatedCode")

/**
 * Blogify resource API service wrappers
 * -------------------------------------
 *
 * Those functions are meant to be used to handle calls managing resources.
 *
 * For example, `fetchWithIdAndRespond` handles a request following these steps :
 *      - Make sure the query URL provides a UUID
 *      - If the calling endpoint specified an authentication predicate, execute it
 *          - If it doesn't pass, respond with 401 Unauthorized / 403 Forbidden
*           - If it does, keep going
*       - Fetch the resource with the provided UUID
 *      - Run a transformation function of type `(Resource) -> Any` if specified by the endpoint
 *      - Handle any errors that occurred during the fetch/transform process
 *          - If an error occurred, respond with an appropriate message
 *          - If no error occurred, respond with the resource.
 *
 * As you can see, those wrappers handle a *lot* of the request process, and cover for many edge cases.
 *
 * If you feel like some request was not handled in a way that seems logical (given that it isn't a problem with business logic),
 * please try to improve the handling of requests at a higher level inside those wrappers first.
 *
 * @author Benjozork, hamza1311, Stan-Sst
 */

package blogify.backend.routes.handling

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Uploadables
import blogify.backend.database.handling.query
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.resources.slicing.PropertyHandle
import blogify.backend.resources.slicing.cachedPropMap
import blogify.backend.resources.slicing.sanitize
import blogify.backend.resources.slicing.slice
import blogify.backend.resources.static.fs.StaticFileHandler
import blogify.backend.resources.static.models.StaticData
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.annotations.type
import blogify.backend.routes.pipelines.CallPipeLineFunction
import blogify.backend.routes.pipelines.CallPipeline
import blogify.backend.routes.pipelines.handleAuthentication
import blogify.backend.routes.pipelines.pipeline
import blogify.backend.routes.pipelines.pipelineError
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.letCatchingOrNull
import blogify.backend.util.matches
import blogify.backend.util.short
import blogify.backend.util.toUUID

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart

import com.github.kittinunf.result.coroutines.SuspendableResult

import com.andreapivetta.kolor.magenta
import com.andreapivetta.kolor.red
import com.andreapivetta.kolor.yellow
import com.github.kittinunf.result.coroutines.failure
import com.github.kittinunf.result.coroutines.map
import org.jetbrains.exposed.sql.deleteWhere

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.UUID

import kotlin.Exception
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

private val logger: Logger = LoggerFactory.getLogger("blogify-service-wrapper")

/**
 * The default predicate used by the wrappers in this file
 */
val defaultPredicateLambda: suspend (user: User, res: Resource) -> Boolean = { _, _ -> true }

/**
 * The default resource-less predicate used by the wrappers in this file
 */
val defaultResourceLessPredicateLambda: suspend (user: User) -> Boolean = { _ -> true }

/**
 * Sends an object describing an exception as a response
 */
suspend fun ApplicationCall.respondExceptionMessage(ex: Service.Exception) {
    respond(HttpStatusCode.InternalServerError, object { @Suppress("unused") val message = ex.message }) // Failure ? Send a simple object with the exception message.
}

fun logUnusedAuth(func: String) {
    logger.debug("${"skipped auth for $func".yellow()} - endpoint didn't request auth")
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching a set of resources with a certain list of desired properties.
 *
 * Requires a [Map] of specific property names to be passed in the query URL.
 *
 * **WARNING:** Those property names must *exactly* match property names present in the class of the specific resource type.
 *Users
 * @param fetch the [function][Function] that retrieves the resources
 *
 * @author hamza1311, Benjozork
 */
@BlogifyDsl
suspend fun <R : Resource> PipelineContext<Unit, ApplicationCall>.fetchAll (
    fetch: suspend (ApplicationCall, Int) -> ResourceResultSet<R>
) {
    val params = call.parameters
    val limit = params["amount"]?.toInt() ?: 25
    val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

    if (selectedPropertyNames == null)
        logger.debug("slicer: getting all fields".magenta())
    else
        logger.debug("slicer: getting fields $selectedPropertyNames".magenta())

    fetch(call, limit).fold (
        success = { resources ->
            try {
                selectedPropertyNames?.let { props ->

                    call.respond(resources.map { it.slice(props) })

                } ?: call.respond(resources.map { it.sanitize() })
            } catch (bruhMoment: Service.Exception) {
                call.respondExceptionMessage(bruhMoment)
            }
        },
        failure = call::respondExceptionMessage
    )
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching a resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param R             the type of [Resource] to be fetched
 * @param fetch         the [function][Function] that retrieves the resource
 * @param authPredicate the [function][Function] that should be run to authenticate the client
 *
 * @author Benjozork, hamza1311
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.fetchWithId (
    fetch:         suspend (ApplicationCall, UUID)  -> ResourceResult<R>,
    authPredicate: suspend (User)                   -> Boolean = defaultResourceLessPredicateLambda
) {
    val params = call.parameters

    params["uuid"]?.let { id -> // Check if the query URL provides any UUID
        val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

        if (selectedPropertyNames == null)
            logger.debug("slicer: getting all fields".magenta())
        else
            logger.debug("slicer: getting fields $selectedPropertyNames".magenta())

        val doFetch: CallPipeLineFunction = {
            fetch.invoke(call, id.toUUID()).fold (
                success = { fetched ->
                    try {
                        selectedPropertyNames?.let { props ->

                            call.respond(fetched.slice(props))

                        } ?: call.respond(fetched.sanitize())
                    } catch (bruhMoment: Service.Exception) {
                        call.respondExceptionMessage(bruhMoment)
                    }
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultResourceLessPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
            runAuthenticated(predicate = authPredicate, block = doFetch)
        } else {
            logUnusedAuth("fetchWithIdAndRespond")
            doFetch(this, Unit)
        }
    } ?: call.respond(HttpStatusCode.BadRequest) // If not, send Bad Request.
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources that are related to a particular resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param R             the type of [Resource] to be fetched
 * @param fetch         the [function][Function] that retrieves the resources using the [ID][UUID] of another resource
 * @param transform     a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.fetchAllWithId (
    fetch:         suspend (UUID) -> ResourceResultSet<R>,
    transform:     suspend (R)    -> Resource = { it },
    authPredicate: suspend (User) -> Boolean = defaultResourceLessPredicateLambda
) {
    val params = call.parameters

    params["uuid"]?.let { id -> // Check if the query URL provides any UUID

        val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

        if (selectedPropertyNames == null)
            logger.debug("slicer: getting all fields".magenta())
        else
            logger.debug("slicer: getting fields $selectedPropertyNames".magenta())

        val doFetch: CallPipeLineFunction = {
            fetch.invoke(id.toUUID()).fold (
                success = { fetchedSet ->
                    if (fetchedSet.isNotEmpty()) {
                        SuspendableResult.of<Set<Resource>, Service.Exception> {
                            fetchedSet.map { transform.invoke(it) }.toSet() // Cover for any errors in transform()
                        }.fold (
                            success = { fetched ->
                                try {
                                    selectedPropertyNames?.let { props ->

                                        call.respond(fetched.map { it.slice(props) })

                                    } ?: call.respond(fetched.map { it.sanitize() })
                                } catch (bruhMoment: Service.Exception) {
                                    call.respondExceptionMessage(bruhMoment)
                                }
                            },
                            failure = call::respondExceptionMessage
                        )
                    } else {
                        call.respond(HttpStatusCode.NoContent)
                    }
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultResourceLessPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
            runAuthenticated(predicate = authPredicate, block = doFetch) // Run provided predicate on authenticated user and provided resource, then run doFetch if the predicate matches
        } else {
            logUnusedAuth("fetchAllWithId")
            doFetch(this, Unit) // Run doFetch without checking predicate
        }

    } ?: call.respond(HttpStatusCode.BadRequest) // If not, send Bad Request.
}

@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> CallPipeline.uploadToResource (
    crossinline fetch:         suspend (ApplicationCall, UUID)   -> ResourceResult<R>,
    crossinline modify:        suspend (R, StaticResourceHandle) -> R,
    crossinline update:        suspend (R)                       -> ResourceResult<*>,
       noinline authPredicate: suspend (User, R)                 -> Boolean = defaultPredicateLambda
) = pipeline("uuid", "target") { (uuid, target) ->

    // Find target resource
    val targetResource = fetch(call, UUID.fromString(uuid))
        .getOrPipelineError(message = "couldn't fetch resource") // Handle result

    handleAuthentication("uploadToResource", { authPredicate(it, targetResource) }) {

        val targetClass = R::class

        // Find target property
        val targetPropHandle = targetClass.cachedPropMap()[target]
            ?.takeIf {
                it is PropertyHandle.Ok
                        && StaticResourceHandle::class.isSuperclassOf(it.property.returnType.classifier as KClass<*>)
            } as? PropertyHandle.Ok
            ?: pipelineError (
                message = "can't find property of type StaticResourceHandle '$target' on class '${targetClass.simpleName}'"
            )

        // Receive data
        val multiPartData = call.receiveMultipart()

        var fileContentType: ContentType = ContentType.Application.Any
        var fileBytes = byteArrayOf()

        multiPartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    part.streamProvider().use { input -> fileBytes = input.readBytes() }
                    fileContentType = part.contentType ?: ContentType.Application.Any
                }
            }
        }

        // Check content type
        val propContentType = targetPropHandle.property.returnType
            .findAnnotation<type>()
            ?.contentType?.letCatchingOrNull(ContentType.Companion::parse) ?: ContentType.Any

        if (!(propContentType matches fileContentType)) {
            pipelineError ( // Throw an error
                HttpStatusCode.UnsupportedMediaType,
                "property '${targetPropHandle.property.name}' of class '${targetClass.simpleName}' does not accept content type '$fileContentType'"
            )
        }

        // Write to file
        val newHandle = StaticFileHandler.writeStaticResource (
            StaticData(fileContentType, fileBytes)
        )

        query {
            Uploadables.insert {
                it[fileId]      = newHandle.fileId
                it[contentType] = newHandle.contentType.toString()
            }
        }.getOrPipelineError(HttpStatusCode.InternalServerError, "error while writing static resource to db")

        // idk - temporary
        val rep = modify(targetResource, newHandle)

        update(rep)
            .getOrPipelineError(HttpStatusCode.InternalServerError, "error while updating resource ${targetResource.uuid.short()} with new information")

        call.respond(newHandle.toString())

    }

}

@BlogifyDsl
suspend inline fun <reified R : Resource> CallPipeline.deleteOnResource (
    crossinline fetch:         suspend (ApplicationCall, UUID)   -> ResourceResult<R>,
       noinline authPredicate: suspend (User, R)                 -> Boolean = defaultPredicateLambda
) = pipeline("uuid", "target") { (uuid, target) ->

    // Find target resource
    val targetResource = fetch(call, UUID.fromString(uuid))
        .getOrPipelineError(message = "couldn't fetch resource") // Handle result

    handleAuthentication("uploadToResource", { authPredicate(it, targetResource) }) {

        val targetClass = R::class

        // Find target property
        val targetPropHandle = targetClass.cachedPropMap()[target]
            ?.takeIf {
                it is PropertyHandle.Ok
                        && StaticResourceHandle::class.isSuperclassOf(it.property.returnType.classifier as KClass<*>)
            } as? PropertyHandle.Ok
            ?: pipelineError (
                message = "can't find property of type StaticResourceHandle '$target' on class '${targetClass.simpleName}'"
            )

        when (val targetPropHandleValue = targetPropHandle.property.get(targetResource) as StaticResourceHandle) {
            is StaticResourceHandle.Ok -> {

                val uploadableId = targetPropHandleValue.fileId

                // Fake handle
                val handle = query {
                    Uploadables.select { Uploadables.fileId eq uploadableId }.single()
                }.map { Uploadables.convert(call, it).get() }.get()


                // Delete in DB
                query {
                    Uploadables.deleteWhere { Uploadables.fileId eq uploadableId }
                }.failure { pipelineError(HttpStatusCode.InternalServerError, "couldn't delete static resource from db") }

                // Delete in FS
                if (StaticFileHandler.deleteStaticResource(handle)) {
                    call.respond(HttpStatusCode.OK)
                } else pipelineError(HttpStatusCode.InternalServerError, "couldn't delete static resource file")

            }
            is StaticResourceHandle.None -> {
                call.respond(HttpStatusCode.NotFound)
                return@handleAuthentication
            }
        }

    }

}

/**
 * Adds a handler to a [CallPipeline] that handles creating a new resource.
 *
 * @param R             the type of [Resource] to be created
 * @param create        the [function][Function] that retrieves that creates the resource using the call
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author Benjozork
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> CallPipeline.createWithResource (
    noinline create:        suspend (R)       -> ResourceResult<R>,
    noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {
    try {

        val rec = call.receive<R>()

        val doCreate: CallPipeLineFunction = {
            val res = create(rec)

            res.fold (
                success = {
                    call.respond(HttpStatusCode.Created)
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
            runAuthenticated(predicate = { u -> authPredicate(u, rec) }, block = doCreate) // Run provided predicate on authenticated user and provided resource, then run doCreate if the predicate matches
        } else {
            logUnusedAuth("createWithResource")
            doCreate(this, Unit) // Run doCreate without checking predicate
        }

    } catch (e: ContentTransformationException) {
        call.respond(HttpStatusCode.BadRequest)
    }
} // KT-33440 | Doesn't compile when lambda called with invoke() for now */

/**
 * Adds a handler to a [CallPipeline] that handles deleting a new resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param fetch         the [function][Function] that retrieves the specified resource. If no [authPredicate] is provided, this is skipped.
 * @param delete        the [function][Function] that deletes the specified resource
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <R: Resource> CallPipeline.deleteWithId (
    fetch:         suspend (ApplicationCall, UUID) -> ResourceResult<R>,
    delete:        suspend (UUID)                  -> ResourceResult<*>,
    authPredicate: suspend (User, R)               -> Boolean = defaultPredicateLambda
) {
    call.parameters["uuid"]?.let { id ->

        val doDelete: CallPipeLineFunction = {
            delete.invoke(id.toUUID()).fold (
                success = {
                    call.respond(HttpStatusCode.OK)
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultPredicateLambda) { // Optimization : fetch is only necessary if a predicate is defined
            fetch.invoke(call, id.toUUID()).fold (
                success = {
                    runAuthenticated(predicate = { u -> authPredicate(u, it)}, block = doDelete) // Run provided predicate on authenticated user and provided resource, then run doDelete if the predicate matches
                }, failure = call::respondExceptionMessage
            )
        } else {
            logUnusedAuth("deleteWithId")
            doDelete(this, Unit) // Run doDelete without checking predicate
        }

    } ?: call.respond(HttpStatusCode.BadRequest)
}
/**
 * Adds a handler to a [CallPipeline] that handles updating a resource with the given uuid.
 *
 * @param R             the type of [Resource] to be created
 * @param update        the [function][Function] that retrieves that creates the resource using the call
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author hamza1311
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> CallPipeline.updateWithId (
    noinline update: suspend (R) -> ResourceResult<*>,
    fetch: suspend (ApplicationCall, UUID) -> ResourceResult<R>,
    noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {

    val replacement = call.receive<R>()

    val doUpdate: CallPipeLineFunction = {
        update(replacement).fold(
            success = {
                call.respond(HttpStatusCode.OK)
            },
            failure = call::respondExceptionMessage
        )
    }
    
    replacement.uuid.let { fetch.invoke(call, it) }.fold(
        success = {
            if (authPredicate != defaultPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
                runAuthenticated (
                    predicate = { u -> authPredicate(u, replacement) },
                    block = doUpdate
                ) // Run provided predicate on authenticated user and provided resource, then run doCreate if the predicate matches
            } else {
                logUnusedAuth("createWithResource")
                doUpdate(this, Unit) // Run doCreate without checking predicate
            }
        },
        failure = call::respondExceptionMessage
    )

}
