package blogify.backend.search

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent

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

    private val typesenseClient = HttpClient { install(JsonFeature) { serializer = JacksonSerializer(); } }

    /**
     * Uploads a document template to the Typesense REST API
     *
     * @param template the document template, in JSON format.
     *                 See the [typesense docs](https://typesense.org/docs/0.11.0/api/#create-collection) for more info.
     *
     * @author hamza1311, Benjozork
     */
    suspend fun submitResourceTemplate(template: String) {
        typesenseClient.use { client ->
            client.post<String> {
                url("$TYPESENSE_URL/collections")
                body = TextContent(template, contentType = ContentType.Application.Json)
                header(TYPESENSE_API_KEY_HEADER, TYPESENSE_API_KEY)
            }.also { println(it) }
        }
    }

}