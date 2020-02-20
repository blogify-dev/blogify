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

package blogify.backend.routing.handling

import blogify.backend.database.Uploadables
import blogify.backend.database.handling.query
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.resources.static.file.StaticFileHandler
import blogify.backend.resources.static.models.StaticData
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.persistence.models.Repository
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.annotations.maxByteSize
import blogify.backend.annotations.type
import blogify.backend.database.ImageUploadablesMetadata
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.resources.reflect.verify
import blogify.backend.resources.static.image.ImageMetadata
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.obtainResources
import blogify.backend.pipelines.handleAuthentication
import blogify.backend.pipelines.optionalParam
import blogify.backend.pipelines.param
import blogify.backend.pipelines.pipelineError
import blogify.backend.search.Typesense
import blogify.backend.util.SrList
import blogify.backend.util.filterThenMapValues
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.letCatchingOrNull
import blogify.backend.util.matches
import blogify.backend.util.reason
import blogify.backend.util.reasons
import blogify.backend.util.short
import blogify.backend.util.toUUID

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.failure
import com.github.kittinunf.result.coroutines.map

import com.andreapivetta.kolor.magenta
import com.andreapivetta.kolor.yellow
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifImageDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.drew.metadata.png.PngDirectory

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.UUID

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

/* temp private */ val logger: Logger = LoggerFactory.getLogger("blogify-service-wrapper")

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
suspend fun ApplicationCall.respondExceptionMessage(ex: Exception) {
    respond(HttpStatusCode.InternalServerError, object { @Suppress("unused") val message = ex.message }) // Failure ? Send a simple object with the exception message.
}

fun logUnusedAuth(func: String) {
    logger.debug("${"skipped auth for $func".yellow()} - endpoint didn't request auth")
}

/**
 * Adds a handler to a [RequestContext] that handles fetching a set of resources with a certain list of desired properties.
 *
 * Requires a [Map] of specific property names to be passed in the query URL.
 *
 * **WARNING:** Those property names must *exactly* match property names present in the class of the specific resource type.
 *
 * @author hamza1311, Benjozork
 */
@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.fetchAllResources() {

    val limit = optionalParam("amount")?.toInt() ?: 25
    val selectedProperties = optionalParam("fields")?.split(",")?.toSet()

    val resources = obtainResources<R>(limit)

    if (selectedProperties == null) {
        logger.debug("slicer: getting all fields".magenta())
        call.respond(resources.map { it.sanitize(excludeUndisplayed = true) })
    } else {
        logger.debug("slicer: getting fields $selectedProperties".magenta())
        call.respond(resources.map { it.slice(selectedProperties) })
    }

}

/**
 * Adds a handler to a [RequestContext] that handles fetching a resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param R             the type of [Resource] to be fetched
 * @param authPredicate the [function][Function] that should be run to authenticate the client
 *
 * @author Benjozork, hamza1311
 */
@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.fetchResource (
    noinline authPredicate: suspend (User) -> Boolean = defaultResourceLessPredicateLambda
) {

    val uuid = param("uuid")
    val selectedProperties = optionalParam("fields")?.split(",")?.toSet()

    handleAuthentication("fetchWithIdAndRespond", authPredicate) {

        val resource = obtainResource<R>(uuid.toUUID())

        if (selectedProperties == null) {
            logger.debug("slicer: getting all fields".magenta())
            call.respond(resource.sanitize(excludeUndisplayed = true))
        } else {
            logger.debug("slicer: getting fields $selectedProperties".magenta())
            call.respond(resource.slice(selectedProperties))
        }

    }

}

/**
 * Adds a handler to a [RequestContext] that handles fetching all the available resources that are related to a particular resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param R          the type of [Resource] to be fetched
 * @param fetch      the [function][Function] that retrieves the resources using the [ID][UUID] of another resource
 * @param transform  a transformation [function][Function] that transforms the [resources][Resource] before sending them back to the client*
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <R : Resource> RequestContext.fetchAllWithId (
    fetch:     suspend (UUID) -> SrList<R>,
    transform: suspend (R) -> Resource = { it }
) {

    val uuid = param("uuid")
    val selectedPropertyNames = optionalParam("fields")?.split(",")?.toSet()

    if (selectedPropertyNames == null)
        logger.debug("slicer: getting all fields".magenta())
    else
        logger.debug("slicer: getting fields $selectedPropertyNames".magenta())

    fetch.invoke(uuid.toUUID()).fold (
        success = { fetchedSet ->
            if (fetchedSet.isNotEmpty()) {
                SuspendableResult.of<Set<Resource>, Repository.Exception> {
                    fetchedSet.map { transform.invoke(it) }.toSet() // Cover for any errors in transform()
                }.fold (
                    success = { fetched ->
                        try {
                            selectedPropertyNames?.let { props ->

                                call.respond(fetched.map { it.slice(props) })

                            } ?: call.respond(fetched.map { it.sanitize(excludeUndisplayed = true) })
                        } catch (bruhMoment: Repository.Exception) {
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

@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.uploadToResource (
       noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {

    val uuid = param("uuid")
    val target = param("target")

    // Find target resource
    val targetResource = obtainResource<R>(uuid.toUUID())

    handleAuthentication("uploadToResource", { authPredicate(it, targetResource) }) {

        val targetClass = R::class

        // Find target property
        val targetPropHandle = targetClass.cachedPropMap()[target]
            ?.takeIf {
                it is PropMap.PropertyHandle.Ok
                        && StaticResourceHandle::class.isSuperclassOf(it.property.returnType.classifier as KClass<*>)
            } as? PropMap.PropertyHandle.Ok
                               ?: pipelineError(
                                   message = "can't find property of type StaticResourceHandle '$target' on class '${targetClass
                                       .simpleName}'"
                               )

        var shouldDelete = false

        // Check if there's already an uploaded file
        val existingValue = targetPropHandle.property.get(targetResource) as StaticResourceHandle
        if (existingValue is StaticResourceHandle.Ok) {
            // Delete later, if successful
            shouldDelete = true
        }

        // Obtain property content type
        val propContentType = targetPropHandle.property.returnType
            .findAnnotation<type>()
            ?.contentType?.letCatchingOrNull(ContentType.Companion::parse) ?: ContentType.Any

        // Obtain property max size
        val propMaxByteSize = targetPropHandle.property.returnType
            .findAnnotation<maxByteSize>()
            ?.value ?: Long.MAX_VALUE

        // Receive data
        val multiPartData = call.receiveMultipart()

        var fileContentType: ContentType = ContentType.Application.Any
        var fileBytes = byteArrayOf()

        multiPartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    if (part.headers["Content-Length"]?.toInt() ?: 0 > propMaxByteSize) { // Check data size
                        call.respond(HttpStatusCode.PayloadTooLarge, reason("file is too large"))
                    } else {
                        // Receive data
                        part.streamProvider().use { input -> fileBytes = input.readBytes() }

                        if (fileBytes.size > propMaxByteSize) { // Check data size again
                            call.respond(HttpStatusCode.BadRequest, reasons("Content-Length header incorrect", "file is too large"))
                        } else {
                            fileContentType = part.contentType ?: ContentType.Application.Any
                        }
                    }
                }
            }
        }

        if (fileContentType matches propContentType) {

            // Write to file
            val newHandle = StaticFileHandler.writeStaticResource (
                StaticData(fileContentType, fileBytes)
            )

            // Write to db
            query {
                Uploadables.insert {
                    it[fileId]      = newHandle.fileId
                    it[contentType] = newHandle.contentType.toString()
                }
            }.getOrPipelineError(HttpStatusCode.InternalServerError, "error while writing static resource to db")

            // Is it an image ? If so, create and store metadata
            if (fileContentType matches ContentType.Image.Any) {
                val metadata = withContext(Dispatchers.IO) { ImageMetadataReader.readMetadata(fileBytes.inputStream()) }

                var imageWidth = 0
                var imageHeight = 0

                letCatchingOrNull {

                    when {
                        fileContentType matches ContentType.Image.PNG -> {
                            val pngMeta = metadata.getFirstDirectoryOfType(PngDirectory::class.java)

                            imageWidth = pngMeta.getInt(PngDirectory.TAG_IMAGE_WIDTH)
                            imageHeight = pngMeta.getInt(PngDirectory.TAG_IMAGE_HEIGHT)
                        }
                        fileContentType matches ContentType.Image.JPEG -> {
                            val jpegMeta = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)

                            imageWidth = jpegMeta.getInt(JpegDirectory.TAG_IMAGE_WIDTH)
                            imageHeight = jpegMeta.getInt(JpegDirectory.TAG_IMAGE_HEIGHT)
                        }
                        else -> {
                            val exif = metadata.getFirstDirectoryOfType(ExifImageDirectory::class.java)
                                       ?: pipelineError(
                                           HttpStatusCode.UnsupportedMediaType,
                                           "image must be png, jpeg or contain exif"
                                       )

                            imageWidth = exif.getInt(ExifImageDirectory.TAG_IMAGE_WIDTH)
                            imageHeight = exif.getInt(ExifImageDirectory.TAG_IMAGE_HEIGHT)
                        }
                    }

                } ?: pipelineError(HttpStatusCode.BadRequest, "invalid metadata in image")


                val imageMetadata = ImageMetadata(imageWidth, imageHeight)

                query {
                    ImageUploadablesMetadata.insert {
                        it[handleId] = newHandle.fileId
                        it[width]    = imageMetadata.width
                        it[height]   = imageMetadata.height
                    }
                }.getOrPipelineError(HttpStatusCode.InternalServerError, "error while writing image metadata to db")
            }

            repository<R>().update(this, targetResource, mapOf(targetPropHandle to newHandle))
                .getOrPipelineError(HttpStatusCode.InternalServerError, "error while updating resource ${targetResource.uuid.short()} with new information")

            call.respond(newHandle.toString())

            // Since at this point it was successful, we can delete
            if (shouldDelete) {
                val idToDelete = (existingValue as StaticResourceHandle.Ok).fileId

                // Delete in DB

                query {
                    Uploadables.deleteWhere { Uploadables.fileId eq idToDelete }
                }.getOrPipelineError(HttpStatusCode.InternalServerError, "could not delete stale static resource $idToDelete from db")

                // Delete in FS

                if (!StaticFileHandler.deleteStaticResource(existingValue)) {
                    pipelineError(HttpStatusCode.InternalServerError, "couldn't delete stale static resource file")
                }

            }

        } else {
            pipelineError( // Throw an error
                HttpStatusCode.UnsupportedMediaType,
                "property '${targetPropHandle.property.name}' of class '${targetClass
                    .simpleName}' does not accept content type '$fileContentType'"
            )
        }

    }

}

@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.deleteUpload (
    noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {

    val uuid = param("uuid")
    val target = param("target")

    // Find target resource
    val targetResource = obtainResource<R>(uuid.toUUID())

    handleAuthentication("uploadToResource", { authPredicate(it, targetResource) }) {

        val targetClass = R::class

        // Find target property
        val targetPropHandle = targetClass.cachedPropMap()[target]
            ?.takeIf {
                it is PropMap.PropertyHandle.Ok
                        && StaticResourceHandle::class.isSuperclassOf(it.property.returnType.classifier as KClass<*>)
            } as? PropMap.PropertyHandle.Ok
                               ?: pipelineError(
                                   message = "can't find property of type StaticResourceHandle '$target' on class '${targetClass
                                       .simpleName}'"
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
                }.failure {
                    pipelineError(HttpStatusCode.InternalServerError, "couldn't delete static resource from db")
                }

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
 * Adds a handler to a [RequestContext] that handles creating a new resource.
 *
 * @param R             the type of [Resource] to be created
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author Benjozork, hamza1311
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.createResource (
    noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {
    try {

        val received = call.receive<R>() // Receive a resource from the request body

        val firstInvalidValue = received.verify().entries.firstOrNull { !it.value }
        if (firstInvalidValue != null) { // Check for any invalid data
            pipelineError(HttpStatusCode.BadRequest, "invalid value for property '${firstInvalidValue.key.name}'")
        }

        handleAuthentication(predicate = { u -> authPredicate(u, received) }) {
            repository<R>().add(received).fold (
                success = {
                    call.respond(HttpStatusCode.Created, it.sanitize(excludeUndisplayed = true))
                    launch { Typesense.uploadResource(it) }
                },
                failure = call::respondExceptionMessage
            )
        }

    } catch (e: ContentTransformationException) {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Adds a handler to a [RequestContext] that handles deleting a new resource.
 *
 * Requires a [UUID] to be passed in the query URL.
 *
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author Benjozork, hamza1311
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R: Resource> RequestContext.deleteResource (
    noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {

    val uuid = param("uuid")

    val toDelete = obtainResource<R>(uuid.toUUID())

    handleAuthentication (
        funcName  = "deleteWithId",
        predicate = { user -> authPredicate(user, toDelete) }
    ) {
        repository<R>().delete(toDelete).fold (
            success = {
                call.respond(HttpStatusCode.OK)
                launch { Typesense.deleteResource<R>(toDelete.uuid) }
            },
            failure = call::respondExceptionMessage
        )
    }

}

/**
 * Adds a handler to a [RequestContext] that handles updating a resource with the given uuid.
 *
 * @param R             the type of [Resource] to be updated
 * @param authPredicate the [function][Function] that should be run to authenticate the client. If omitted, no authentication is performed.
 *
 * @author hamza1311
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.updateResource (
    noinline authPredicate: suspend (User, R) -> Boolean = defaultPredicateLambda
) {

    val replacement = call.receive<Map<String, Any>>()
    val current = obtainResource<R>((replacement["uuid"] as String).toUUID())

    val rawData = replacement.mapKeys { n -> R::class.cachedPropMap().ok().values.first { it.name == n.key } }

    handleAuthentication (
        funcName  = "createWithResource",
        predicate = { user -> authPredicate(user, current) }
    ) {
        repository<R>().update(this, current, rawData).fold (
            success = {
                call.respond(HttpStatusCode.OK)
                launch { Typesense.updateResource(it) }
            },
            failure = { e ->
                e.printStackTrace()
            }
        )
    }

}

/**
 * Adds a handler to a [RequestContext] that returns the validation regexps for a certain class.
 *
 * @param M the class for which to return validations
 *
 * @author Benjozork
 */
suspend inline fun <reified M : Mapped> RequestContext.getValidations() {
    call.respond (
        M::class.cachedPropMap().ok()
            .filterThenMapValues (
                { it.regexCheck != null },
                { it.value.regexCheck!!.pattern }
            )
    )
}
