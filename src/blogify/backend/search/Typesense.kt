package blogify.backend.search

import blogify.backend.resources.models.Resource
import blogify.backend.search.ext._template
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

import com.andreapivetta.kolor.green
import io.ktor.client.request.get

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("blogify-typesense-client")

/**
 * Meta object regrouping setup and utility functions for the Typesense search engine.
 */
object Typesense {

    /**
     * Typesense REST API URL
     */
    private const val TYPESENSE_URL = "http://ts:8108"

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

    val typesenseClient = HttpClient { install(JsonFeature) { serializer = JacksonSerializer(); } }

    /**
     * Uploads a document template to the Typesense REST API
     *
     * @param template the document template, in JSON format.
     *                 See the [typesense docs](https://typesense.org/docs/0.11.0/api/#create-collection) for more info.
     *
     * @author hamza1311, Benjozork
     */
    suspend fun <R : Resource> submitResourceTemplate(template: Template<R>) {
        typesenseClient.post<HttpResponse> {
            url("$TYPESENSE_URL/collections")
            header(TYPESENSE_API_KEY_HEADER, TYPESENSE_API_KEY)
            contentType(ContentType.Application.Json)

            body = template
        }.let { response ->
            when (val status = response.status) {
                HttpStatusCode.Created,
                HttpStatusCode.Conflict -> { // Both of those cases mean the template either already exists or was created
                    logger.info("uploaded Typesense template '${template.name}'".green())
                }
                else -> error("error while uploading Typesense template: '${status.value} ${status.description}'")
            }
        }
    }

    suspend inline fun <reified R : Resource> search(query: String): Search<R> {
        val template = R::class._template
        return typesenseClient.get {
            val excludedFieldsString = template.fields.joinToString(separator = ",") { it.name }
            url("http://ts:8108/collections/articles/documents/search?q=$query&query_by=content,title&exclude_fields=$excludedFieldsString")
        }
    }

}