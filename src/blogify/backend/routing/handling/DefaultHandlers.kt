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

import blogify.common.util.*
import blogify.common.util.filterThenMapValues
import blogify.reflect.*
import blogify.reflect.computed.models.BasicComputedProperty
import blogify.reflect.computed.resolveComputedProps
import blogify.reflect.extensions.klass
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok
import blogify.reflect.entity.mappedByHandles
import blogify.database.handling.query
import blogify.backend.database.tables.Uploadables
import blogify.backend.resources.user.User
import blogify.backend.entity.Resource
import blogify.backend.resources.static.file.StaticFileHandler
import blogify.backend.resources.static.models.StaticData
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.annotations.maxByteSize
import blogify.backend.annotations.type
import blogify.backend.auth.handling.maybeAuthenticated
import blogify.backend.auth.handling.optionallyAuthenticated
import blogify.backend.database.tables.ImageUploadablesMetadata
import blogify.backend.pipelines.*
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.static.image.ImageMetadata
import blogify.backend.resources.Article
import blogify.backend.resources.reflect.*
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.util.*

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

import com.github.kittinunf.result.coroutines.failure
import com.github.kittinunf.result.coroutines.map

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifImageDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.drew.metadata.png.PngDirectory

import org.jetbrains.exposed.sql.*

import java.util.UUID

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

import kotlinx.coroutines.*

/**
 * Takes a predicate of type `suspend (User, R) -> Boolean` and captures it into a returned predicate of type
 * `suspend (User) -> Boolean`.
 *
 * @author Benjozork
 */
fun <R> wrapPredicate(basePredicate: (suspend (User, R) -> Boolean)?, resource: R): (suspend (User) -> Boolean)? {
    return if (basePredicate != null) {
        { u -> basePredicate(u, resource) }
    } else null
}

/**
 * Sends an object describing an exception as a response
 */
suspend fun ApplicationCall.respondExceptionMessage(ex: Exception) {
    respond(HttpStatusCode.InternalServerError, object { @Suppress("unused") val message = ex.message }) // Failure ? Send a simple object with the exception message.
}

fun applyDefaultComputedPropertyResolver(resource: Mapped, user: User?) {
    resolveComputedProps(resource) { container ->
        when (container) {
            is BasicComputedProperty ->
                container.function()
            is UserSpecificPropertyContainer ->
                user?.let { container.resolve(resource, it) }
            else -> never
        }
    }
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

    optionallyAuthenticated { user ->
        for (resource in resources) {
            applyDefaultComputedPropertyResolver(resource, user)
        }

        if (selectedProperties == null) {
            call.respond(resources.map { it.sanitize(excludeUndisplayed = true) })
        } else {
            call.respond(resources.map { it.slice(selectedProperties) })
        }
    }
}

@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.fetchResourceListing (
    orderBy: Column<*>,
    sortOrder: SortOrder,
    noinline selectCondition: SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
) {
    val repo = repository<R>()
    val quantity = param("quantity").toIntOrNull() ?: 15
    val page = param("page").toIntOrNull() ?: 0
    val selectedPropertyNames = optionalParam("fields")?.split(",")?.toSet()

    val results = repo.obtainListing(selectCondition, quantity, page, orderBy, sortOrder)
        .getOr404OrPipelineError(HttpStatusCode.InternalServerError, "couldn't query listing")

    optionallyAuthenticated { user ->
        for (resource in results.first) {
            applyDefaultComputedPropertyResolver(resource, user)
        }

        call.respond(object {
            val data = results.first.map {
                selectedPropertyNames?.let { props -> it.slice(props) } ?: it.sanitize()
            }
            val moreAvailable = results.second
        })
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
    noinline authPredicate: (suspend (User) -> Boolean)? = null
) {
    val uuid by queryUuid
    val selectedProperties = optionalParam("fields")?.split(",")?.toSet()

    optionallyAuthenticated(authPredicate) { user ->
        val resource = obtainResource<R>(uuid)

        applyDefaultComputedPropertyResolver(resource, user)

        if (selectedProperties == null) {
            call.respond(resource.sanitize(excludeUndisplayed = true))
        } else {
            call.respond(resource.slice(selectedProperties))
        }
    }
}

@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
@BlogifyDsl
suspend inline fun <reified R : Resource> RequestContext.uploadToResource (
    noinline authPredicate: (suspend (User, R) -> Boolean)? = null
) {
    val uuid by queryUuid
    val target = param("target")

    // Find target resource
    val targetResource = obtainResource<R>(uuid)

    maybeAuthenticated(wrapPredicate(authPredicate, targetResource)) {

        val targetClass = R::class

        // Find target property
        val targetPropHandle = targetClass.propMap[target]
            ?.takeIf {
                it is PropMap.PropertyHandle.Ok
                        && StaticFile::class.isSuperclassOf(it.property.returnType.klass() ?: never)
            } as? PropMap.PropertyHandle.Ok
            ?: pipelineError(
                message = "can't find property of type StaticResourceHandle '$target' on class '${targetClass
                    .simpleName}'"
            )

        var shouldDelete = false

        // Check if there's already an uploaded file
        val existingValue = targetPropHandle.property.get(targetResource) as StaticFile
        if (existingValue is StaticFile.Ok) {
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
                else -> call.respond(HttpStatusCode.BadRequest, reason("invalid part type '${part::class.simpleName}'"))
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
                    it[fileId] = newHandle.fileId
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
                                ?: pipelineError (
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
                        it[width] = imageMetadata.width
                        it[height] = imageMetadata.height
                    }
                }.getOrPipelineError (
                    HttpStatusCode.InternalServerError,
                    "error while writing image metadata to db"
                )
            }

            repository<R>().update(this, targetResource, mapOf(targetPropHandle to newHandle))
                .getOrPipelineError (
                    HttpStatusCode.InternalServerError,
                    "error while updating resource ${targetResource.uuid.short()} with new information"
                )

            call.respond(newHandle.toString())

            // Since at this point it was successful, we can delete
            if (shouldDelete) {
                val idToDelete = (existingValue as StaticFile.Ok).fileId

                // Delete in DB

                query {
                    Uploadables.deleteWhere { Uploadables.fileId eq idToDelete }
                }.getOrPipelineError (
                    HttpStatusCode.InternalServerError,
                    "could not delete stale static resource $idToDelete from db"
                )

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
    noinline authPredicate: (suspend (User, R) -> Boolean)? = null
) {
    val uuid by queryUuid
    val target = param("target")

    // Find target resource
    val targetResource = obtainResource<R>(uuid)

    maybeAuthenticated(wrapPredicate(authPredicate, targetResource)) {

        val targetClass = R::class

        // Find target property
        val targetPropHandle = targetClass.propMap[target]
            ?.takeIf {
                it is PropMap.PropertyHandle.Ok
                        && StaticFile::class.isSuperclassOf(it.property.returnType.klass() ?: never)
            } as? PropMap.PropertyHandle.Ok
            ?: pipelineError(
                message = "can't find property of type StaticResourceHandle '$target' on class '${targetClass
                    .simpleName}'"
            )

        when (val targetPropHandleValue = targetPropHandle.property.get(targetResource) as StaticFile) {
            is StaticFile.Ok -> {

                val uploadableId = targetPropHandleValue.fileId

                // Fake handle
                val handle = query {
                    Uploadables.select { Uploadables.fileId eq uploadableId }.single()
                }.map { Uploadables.convert(this, it).get() }.get()

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
            is StaticFile.None -> {
                call.respond(HttpStatusCode.NotFound)
                return@maybeAuthenticated
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
    noinline authPredicate: (suspend (User, R) -> Boolean)? = null
) {
    try {

        val received = call.receive<Dto>() // Receive a resource from the request body
            .mappedByHandles(R::class, false)
            .getOrPipelineError(HttpStatusCode.BadRequest, "bad DTO format")
            .let { data -> R::class.construct(data) }
            .getOrPipelineError(HttpStatusCode.BadRequest, "could not instantiate resource")

        val firstInvalidValue = received.verify().entries.firstOrNull { !it.value }
        if (firstInvalidValue != null) { // Check for any invalid data
            pipelineError(HttpStatusCode.BadRequest, "invalid value for property '${firstInvalidValue.key.name}'")
        }

        maybeAuthenticated(wrapPredicate(authPredicate, received)) { user ->
            repository<R>().add(received).fold (
                success = { resource ->
                    applyDefaultComputedPropertyResolver(resource, user)

                    call.respond(HttpStatusCode.Created, resource.sanitize(excludeUndisplayed = true))

                    launch { // Dispatch creation events and call creation function
                        resource.onCreation(this@createResource)
                    }

                    /*launch {
                        Typesense.uploadResource(it)
                    }*/
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
    noinline authPredicate: (suspend (User, R) -> Boolean)? = null
) {
    val uuid by queryUuid
    val toDelete = obtainResource<R>(uuid)

    maybeAuthenticated(wrapPredicate(authPredicate, toDelete)) {
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
    noinline authPredicate: (suspend (User, R) -> Boolean)? = null
) {
    val replacement = call.receive<Dto>()
    val id = (replacement["uuid"] as? String ?: call.parameters["uuid"])?.toUUIDOrNull()
        ?: pipelineError(HttpStatusCode.BadRequest, "resource uuid not found in update object or url")

    val current = obtainResource<R>(id)

    val rawData = replacement.mappedByHandles(R::class)
        .getOr404OrPipelineError(HttpStatusCode.BadRequest, "bad update object (invalid properties)")

    maybeAuthenticated(wrapPredicate(authPredicate, current)) {
        val updatedResource = repository<R>().update(this, current, rawData)
            .getOrPipelineError(message = "couldn't update resource")

        call.respond(HttpStatusCode.OK)

        launch { Typesense.updateResource(updatedResource) }
    }

}

suspend inline fun <reified R : Resource> RequestContext.search(filters: Map<PropMap.PropertyHandle.Ok, Any> = emptyMap()) {
    val query = param("q")
    val view = Typesense.search<Article>(query, filters).asSearchView(this)
    call.respond(view)
}

/**
 * Adds a handler to a [RequestContext] that returns the validation regexps for a certain class.
 *
 * @param M the class for which to return validations
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend inline fun <reified M : Mapped> RequestContext.getValidations() {
    call.respond (
        M::class.propMap.ok
            .filterThenMapValues (
                { it.regexCheck != null },
                { it.value.regexCheck!!.pattern }
            )
    )
}
