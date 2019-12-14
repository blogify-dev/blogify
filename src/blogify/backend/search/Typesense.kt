package blogify.backend.search

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.sanitize
import blogify.backend.routes.pipelines.pipelineError
import blogify.backend.search.ext._rebuildSearchTemplate
import blogify.backend.search.ext._searchTemplate
import blogify.backend.search.models.Search
import blogify.backend.search.models.Template
import blogify.backend.util.service
import blogify.backend.util.short

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.client.call.receive
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.http.content.TextContent

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule

import com.andreapivetta.kolor.green
import com.andreapivetta.kolor.red

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.UUID

val tscLogger: Logger = LoggerFactory.getLogger("blogify-typesense-client")

/**
 * Meta object regrouping setup and utility functions for the Typesense search engine.
 */
object Typesense {

    /**
     * Typesense REST API URL
     */
    const val TYPESENSE_URL = "http://ts:8108"

    /**
     * Typesense API key HTTP header string
     */
    private const val TYPESENSE_API_KEY_HEADER = "X-TYPESENSE-API-KEY"

    /**
     * Typesense API key
     *
     * TODO use an env variable instead
     */
    private const val TYPESENSE_API_KEY = "Hu52dwsas2AdxdE"

    lateinit var objectMapper: ObjectMapper
    private val typesenseSerializer = JacksonSerializer {
        // Register a serializer for Resource.
        // This will only affect pure Resource objects, so elements produced by the slicer are not affected,
        // since those don't use Jackson for root serialization.

        val blogifyModule = SimpleModule()
        blogifyModule.addSerializer(Resource.ResourceIdSerializer)
        registerModule(blogifyModule)

        objectMapper = this // Capture the objectMapper
    }

    val typesenseClient = HttpClient {
        install(JsonFeature) {
            serializer = typesenseSerializer
        }

        // Always include Typesense headers
        defaultRequest {
            header(TYPESENSE_API_KEY_HEADER, TYPESENSE_API_KEY)
        }

        // Allows us to read response even when status < 300
        expectSuccess = false
    }

    /**
     * Builds the document to be sent to typesense.
     * Checks if delegation exists. If it does, returns the delegated result; original value if it doesn't
     *
     * @return The sanitized document ready for typesense
     *
     * @author Benjozork
     */
    inline fun <reified R : Resource> makeDocument(resource: R): Map<String, Any?> {
        val template = R::class._searchTemplate

        return (resource.sanitize(noSearch = true) + ("id" to resource.uuid)).entries
            .map {
                it.key to (
                    template.delegatedFields
                        .firstOrNull { df -> df.name == it.key } // Check if we have a delegated field
                        ?.let { df -> df.delegatedTo!!.get(it.value) } ?: it.value
                    // Return the delegation result if there is; the original value if there is not.
                )
            }.toMap()
    }

    /**
     * Get a message from a Typesense error response
     */
    suspend fun typesenseMessage(response: HttpResponse) = response.receive<Map<String, Any?>>()["message"] as? String

    /**
     * Uploads a document template to the Typesense REST API
     *
     * @param R        class associated with [template]
     * @param template the document template.
     *                 See the [typesense docs](https://typesense.org/docs/0.11.0/api/#create-collection) for more info.
     *
     * @author hamza1311, Benjozork
     */
    suspend fun <R : Resource> submitResourceTemplate(template: Template<R>) {
        typesenseClient.post<HttpResponse> {
            url("$TYPESENSE_URL/collections")
            contentType(ContentType.Application.Json)

            body = template
        }.let { response ->
            when (response.status) {
                HttpStatusCode.Created,
                HttpStatusCode.Conflict -> { // Both of those cases mean the template either already exists or was created
                    tscLogger.info("uploaded Typesense template '${template.name}'".green())
                }
                else -> {
                    error("error while uploading Typesense template: ${template.name} ${typesenseMessage(response)}")
                }
            }
        }
    }

    /**
     * Adds a [Resource] to the [Typesense] index
     *
     * @param R        the class of the resource to upload
     * @param resource the resource to upload
     *
     * @author Benjozork
     */
    suspend inline fun <reified R : Resource> uploadResource(resource: R) {
        val template = R::class._searchTemplate

        typesenseClient.post<HttpResponse> {
            url("$TYPESENSE_URL/collections/${template.name}/documents")
            contentType(ContentType.Application.Json)

            body = makeDocument(resource)
        }.let { response ->
            if (response.status.isSuccess()) {
                tscLogger.trace("uploaded resource ${resource.uuid.short()} to Typesense index".green())
            } else {
                tscLogger.error("couldn't upload resource ${resource.uuid.short()} to Typesense index: ${typesenseMessage(response)}".red())
            }
        }
    }

    /**
     * Removes a [Resource] from the [Typesense] index
     *
     * @param R  the class of the resource to delete
     * @param id the id resource to delete
     *
     * @author Benjozork
     */
    suspend inline fun <reified R : Resource> deleteResource(id: UUID) {
        val template = R::class._searchTemplate

        typesenseClient.delete<HttpResponse> {
            url("$TYPESENSE_URL/collections/${template.name}/documents/$id")
        }.let { response ->
            if (response.status.isSuccess()) {
                tscLogger.trace("deleted resource ${id.short()} from Typesense index".green())
            } else {
                tscLogger.error("couldn't delete resource ${id.short()} from Typesense index: ${typesenseMessage(response)}".red())
            }
        }
    }

    /**
     * Updates a [Resource] in the [Typesense] index
     *
     * @param R        the class of the resource to update
     * @param resource the resource to replace the previous resource of the same UUID with
     *
     * @author Benjozork
     */
    suspend inline fun <reified R : Resource> updateResource(resource: R) {
        deleteResource<R>(resource.uuid)
        uploadResource(resource)
    }

    /**
     * Executes a search [query] for resources of type [R]
     *
     * @param R     the type of resources to search for
     * @param query the search query to use
     *
     * @return a [Search] containing the results
     *
     * @author Benjozork
     */
    suspend inline fun <reified R : Resource> search (
        query:   String,
        filters: Map<PropMap.PropertyHandle.Ok, Any> = emptyMap()
    ): Search<R> {
        val template = R::class._searchTemplate
        val excludedFieldsString = template.fields
            .joinToString(separator = ",") { it.name }
        val filtersString = filters.takeIf { it.isNotEmpty() }?.entries
            ?.joinToString(separator = "&&") { "${it.key.name}:${it.value}" }

        return typesenseClient.get<HttpResponse> {
            url (
                TYPESENSE_URL +
                "/collections/${template.name}" +
                "/documents/search?q=$query" +
                "&query_by=${template.queryByParams}" +
                "&exclude_fields=$excludedFieldsString" +
                if (filtersString != null) "&filter_by=$filtersString" else ""
            )
        }.let { response ->
            if (response.status.isSuccess()) {
                return@let response.receive<Search<R>>()
            } else {
                tscLogger.error("couldn't search in Typesense index ${template.name}: ${typesenseMessage(response)}".red())
                pipelineError(HttpStatusCode.InternalServerError, "error during Typesense search")
            }
        }
    }

    /**
     * Refreshes typesense index. It sends the following requests:
     * * [Drop collection][deleteCollection]
     * * Rebuild the search template
     * * Submit the aforementioned template
     * * [Bulk uploads][bulkUploadResources] the resources
     *
     * @param R The [Resource] whose corresponding index is to be refreshed
     *
     * @return The [HttpResponse] of the request
     *
     * @author hamza1311
     */
    suspend inline fun <reified R: Resource> refreshIndex(): HttpResponse {
        val resources = R::class.service.getAll().get()
        val docs = resources.map { makeDocument(it) }

        deleteCollection<R>()
        submitResourceTemplate(R::class._rebuildSearchTemplate())

        return bulkUploadResources<R>(docs)
    }

    /**
     * Uploads [resources][Resource] to typesense in bulk.
     * A document import request is sent to typesense.
     *
     * @param documents The documents to be imported. These are converted into a format typesense can understand before sending the request
     *
     * @return The [HttpResponse] of the request
     *
     * @author Benjozork, hamza1311
     */
    suspend inline fun <reified R : Resource> bulkUploadResources(documents: List<Map<String, Any?>>): HttpResponse {
        val template = R::class._searchTemplate

        return typesenseClient.post {
            url("$TYPESENSE_URL/collections/${template.name}/documents/import")
            body = TextContent (
                documents.joinToString(separator = "\n") {
                    objectMapper.writeValueAsString(it).replace("\n", " ")
                }, ContentType.Text.Plain)
        }
    }

    /**
     * Drops a typesense collection
     * @param R The resource whose collection is to be dropped
     *
     * @return The [HttpResponse] of the request
     *
     * @author hamza1311
     */
    suspend inline fun <reified R : Resource> deleteCollection(): HttpResponse {
        val template = R::class._searchTemplate

        return typesenseClient.delete {
            url("$TYPESENSE_URL/collections/${template.name}/")
        }
    }
}
