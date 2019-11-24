package blogify.backend.search

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.sanitize
import blogify.backend.search.ext._searchTemplate
import blogify.backend.search.models.Search
import blogify.backend.search.models.Template

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
import io.ktor.client.call.receive
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get

import com.andreapivetta.kolor.green

import org.slf4j.LoggerFactory

val tscLogger = LoggerFactory.getLogger("blogify-typesense-client")

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

    val typesenseClient = HttpClient {
        install(JsonFeature) { serializer = JacksonSerializer(); }

        // Always include Typesense headers
        defaultRequest {
            header(TYPESENSE_API_KEY_HEADER, TYPESENSE_API_KEY)
        }

        // Allows us to read response even when status < 300
        expectSuccess = false
    }

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
            when (val status = response.status) {
                HttpStatusCode.Created,
                HttpStatusCode.Conflict -> { // Both of those cases mean the template either already exists or was created
                    tscLogger.info("uploaded Typesense template '${template.name}'".green())
                }
                else -> {
                    val bodyMessage = response.receive<Map<String, Any?>>()["message"] as? String
                    error("error while uploading Typesense template '${template.name}': '${status.value} ${status.description} / ${bodyMessage}'")
                }
            }
        }
    }

    suspend inline fun <reified R : Resource> uploadResource(resource: R) {
        val template = R::class._searchTemplate

        typesenseClient.post<HttpResponse> {
            url("$TYPESENSE_URL/collections/${template.name}/documents")
            contentType(ContentType.Application.Json)

            body = resource.sanitize(noSearch = true)
        }.let { response ->
            val bodyMessage = response.receive<Map<String, Any?>>()["message"] as? String
            tscLogger.trace("${response.status} $bodyMessage")
        }
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
    suspend inline fun <reified R : Resource> search(query: String): Search<R> {
        val template = R::class._searchTemplate
        val excludedFieldsString = template.fields.joinToString(separator = ",") { it.name }

        return typesenseClient.get {
            url("$TYPESENSE_URL/collections/${template.name}/documents/search?q=$query&query_by=content,title&exclude_fields=$excludedFieldsString")
        }
    }

}