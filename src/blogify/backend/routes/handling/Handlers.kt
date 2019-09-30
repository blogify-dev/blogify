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

import blogify.backend.auth.handling.authenticatedBy
import blogify.backend.database.ResourceTable
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.util.BlogifyDsl
import blogify.backend.util.toUUID

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive

import com.github.kittinunf.result.coroutines.SuspendableResult

import com.andreapivetta.kolor.magenta
import com.andreapivetta.kolor.yellow

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.UUID

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

val logger: Logger = LoggerFactory.getLogger("blogify-service-wrapper")

/**
 * Represents a server call pipeline
 */
typealias CallPipeline = PipelineContext<Unit, ApplicationCall>

/**
 * Represents a server call handler function
 */
typealias CallPipeLineFunction = PipelineInterceptor<Unit, ApplicationCall>

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
 * Adds a handler to a [CallPipeline] that handles fetching a resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param R             the type of [Resource] to be fetched
 * @param fetch         the [function][Function] that retrieves the resource
 * @param transform     a transformation [function][Function] that transforms the [resource][Resource] before sending it back to the client
 * @param authPredicate the [function][Function] that should be run to authenticate the client
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.fetchWithIdAndRespond (
    fetch:         suspend (id: UUID)   -> ResourceResult<R>,
    transform:     suspend (fetched: R) -> Any = { it },
    authPredicate: suspend (user: User) -> Boolean = defaultResourceLessPredicateLambda
) {
    call.parameters["uuid"]?.let { id -> // Check if the query URL provides any UUID

        val wrapFetchFunc: CallPipeLineFunction = {
            fetch.invoke(id.toUUID()).fold (
                success = { fetched ->
                    SuspendableResult.of<Any, Service.Exception> {
                        transform.invoke(fetched) // Cover for any errors in transform()
                    }.fold (
                        success = call::respond,
                        failure = call::respondExceptionMessage
                    )
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultResourceLessPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
            authenticatedBy(predicate = authPredicate, block = wrapFetchFunc)
        } else {
            logUnusedAuth("fetchWithIdAndRespond")
            wrapFetchFunc(this, Unit)
        }
    } ?: call.respond(HttpStatusCode.BadRequest) // If not, send Bad Request.
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching all the available resources.
 *
 * @param R             the type of [Resource] to be fetched
 * @param fetch         the [function][Function] that retrieves the resources
 * @param transform     a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <R : Resource> CallPipeline.fetchAndRespondWithAll (
    fetch:         suspend ()           -> ResourceResultSet<R>,
    transform:     suspend (elem: R)    -> Any = { it },
    authPredicate: suspend (user: User) -> Boolean = defaultResourceLessPredicateLambda
) {
    val wrapFetchAllFunc: CallPipeLineFunction = {
        fetch.invoke().fold (
            success = { fetchedSet ->
                if (fetchedSet.isNotEmpty()) {
                    SuspendableResult.of<Set<Any>, Service.Exception> {
                        fetchedSet.map { transform.invoke(it) }.toSet() // Cover for any errors in transform()
                    }.fold (
                        success = call::respond,
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
        authenticatedBy(predicate = authPredicate, block = wrapFetchAllFunc)
    } else {
        logUnusedAuth("fetchAndRespondWithAll")
        wrapFetchAllFunc(this, Unit)
    }
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
suspend fun <R : Resource> CallPipeline.handleIdentifiedResourceFetchAll (
    fetch:         suspend (id: UUID)   -> ResourceResultSet<R>,
    transform:     suspend (elem: R)    -> Any = { it },
    authPredicate: suspend (user: User) -> Boolean = defaultResourceLessPredicateLambda
) {
    call.parameters["uuid"]?.let { id -> // Check if the query URL provides any UUID

        val wrapFetchFunc: CallPipeLineFunction = {
            fetch.invoke(id.toUUID()).fold (
                success = { fetchedSet ->
                    if (fetchedSet.isNotEmpty()) {
                        SuspendableResult.of<Set<Any>, Service.Exception> {
                            fetchedSet.map { transform.invoke(it) }.toSet() // Cover for any errors in transform()
                        }.fold (
                            success = call::respond,
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
            authenticatedBy(predicate = authPredicate, block = wrapFetchFunc) // Run provided predicate on authenticated user and provided resource, then run doFetch if the predicate matches
        } else {
            logUnusedAuth("handleIdentifiedResourceFetchAll")
            wrapFetchFunc(this, Unit) // Run doFetch without checking predicate
        }

    } ?: call.respond(HttpStatusCode.BadRequest) // If not, send Bad Request.
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
    noinline create:        suspend (res: R)             -> ResourceResult<R>,
    noinline authPredicate: suspend (user: User, res: R) -> Boolean = defaultPredicateLambda
) {
    try {

        val rec = call.receive<R>()

        val wrapCreateFunc: CallPipeLineFunction = {
            val res = create(rec)

            res.fold (
                success = {
                    call.respond(HttpStatusCode.Created)
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultPredicateLambda) { // Don't authenticate if the endpoint doesn't authenticate
            authenticatedBy(predicate = { u -> authPredicate(u, rec) }, block = wrapCreateFunc) // Run provided predicate on authenticated user and provided resource, then run doCreate if the predicate matches
        } else {
            logUnusedAuth("createWithResource")
            wrapCreateFunc(this, Unit) // Run doCreate without checking predicate
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
    fetch:         suspend (id: UUID)           -> ResourceResult<R>,
    delete:        suspend (id: UUID)           -> ResourceResult<*>,
    authPredicate: suspend (user: User, res: R) -> Boolean = defaultPredicateLambda
) {
    call.parameters["uuid"]?.let { id ->

        val wrapDeleteFunc: CallPipeLineFunction = {
            delete.invoke(id.toUUID()).fold (
                success = {
                    call.respond(HttpStatusCode.OK)
                },
                failure = call::respondExceptionMessage
            )
        }

        if (authPredicate != defaultPredicateLambda) { // Optimization : fetch is only necessary if a predicate is defined
            fetch.invoke(id.toUUID()).fold (
                success = {
                    authenticatedBy(predicate = { u -> authPredicate(u, it)}, block = wrapDeleteFunc) // Run provided predicate on authenticated user and provided resource, then run doDelete if the predicate matches
                }, failure = call::respondExceptionMessage
            )
        } else {
            logUnusedAuth("deleteWithId")
            wrapDeleteFunc(this, Unit) // Run doDelete without checking predicate
        }

    } ?: call.respond(HttpStatusCode.BadRequest)
}

/**
 * Adds a handler to a [CallPipeline] that handles fetching a set of resources with a certain list of desired properties.
 *
 * Requires a [Map] of specific property names to be passed in the query URL.
 *
 * **WARNING:** Those property names must *exactly* match property names present in the class of the specific resource type.
 *
 * @param fetch the [function][Function] that retrieves the resources
 * @param table the [resource table][ResourceTable] that contains the specified resource in the database
 *
 * @author hamza1311, Benjozork
 */
@BlogifyDsl
suspend fun <R : Resource> PipelineContext<Unit, ApplicationCall>.fetchAndSlideResourceAndRespond (
    fetch: suspend (ResourceTable<R>, Int) -> ResourceResultSet<R>,
    table: ResourceTable<R>
) {
    val params = call.parameters
    val limit = params["amount"]?.toInt() ?: 25
    val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

    if (selectedPropertyNames == null)
        logger.debug("slicer: getting all fields".magenta())
    else
        logger.debug("slicer: getting fields $selectedPropertyNames".magenta())

    fetch(table, limit).fold (
        success = { resources ->
            try {
                selectedPropertyNames?.let {

                    call.respond(sliceResourceSet(resources, it.toSet()))

                } ?: call.respond(resources)
            } catch (bruhMoment: Service.Exception) {
                call.respondExceptionMessage(bruhMoment)
            }
        },
        failure = call::respondExceptionMessage
    )
}

/**
 * Slices a [Set] of [resources][Resource] with a set of provided properties that should be kept
 *
 * @param resources             the [resources][Resource] to be sliced
 * @param selectedPropertyNames the properties that should be kept on the returned [resources][Resource]
 *
 * @return a list of [maps][Map] containing [resources][Resource] with only the provided properties on them
 *
 * @author hamza1311, Benjozork
 */
private fun <R : Resource> sliceResourceSet (
    resources:             Set<R>,
    selectedPropertyNames: Set<String>
): List<Map<String, Any>> {

    /**
     * Reads a property from an instance of [T] with [a certain name][propertyName] using reflection
     *
     * Shamelessly stolen from: [https://stackoverflow.com/a/35539628]
     *
     * @param instance     instance of [T] to read property from
     * @param propertyName name of the property to read
     *
     * @return the value of the property [propertyName] on [instance]
     *
     * @author hamza1311, Benjozork
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Resource, R> getPropValueOnInstance(instance: T, propertyName: String): R {
        val property = instance::class.declaredMemberProperties
            .first { it.name == propertyName } as KProperty1<T, R>

        return property.get(instance)
    }

    return resources.map { res ->
        selectedPropertyNames.associateWith { propName ->
            getPropValueOnInstance<Resource, Any>(res, propName)
        }
    }
}