package blogify.backend.search

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url

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

    sealed class Type(val facet: Boolean, val textForm: kotlin.String) {

        class String      (facet: Boolean = false): Type(facet, "string")
        class StringArray (facet: Boolean = false): Type(facet, "string[]")
        class Int32       (facet: Boolean = false): Type(facet, "int32")
        class Int32Array  (facet: Boolean = false): Type(facet, "int32[]")
        class Int64       (facet: Boolean = false): Type(facet, "int64")
        class Int64Array  (facet: Boolean = false): Type(facet, "int64[]")
        class Float       (facet: Boolean = false): Type(facet, "float")
        class FloatArray  (facet: Boolean = false): Type(facet, "float[]")
        class Bool        (facet: Boolean = false): Type(facet, "bool")
        class BoolArray   (facet: Boolean = false): Type(facet, "bool[]")

        object Serializer : StdSerializer<Type>(Type::class.java) {
            override fun serialize(value: Type?, gen: JsonGenerator?, provider: SerializerProvider?) = gen!!.writeString(value?.textForm)
        }

    }

    class Template<T : Any> (
        val name: String,

        val fields: Map<String, Type>,

        @JsonProperty("default_sorting_field")
        val defaultSortingField: String
    )

    private val typesenseClient = HttpClient { install(JsonFeature) { serializer = JacksonSerializer(); } }

    /**
     * Uploads a document template to the Typesense REST API
     *
     * @param template the document template, in JSON format.
     *                 See the [typesense docs](https://typesense.org/docs/0.11.0/api/#create-collection) for more info.
     *
     * @author hamza1311, Benjozork
     */
    suspend fun <T : Any> submitResourceTemplate(template: Template<T>) {
        typesenseClient.use { client ->
            client.post<String> {
                url("$TYPESENSE_URL/collections")
                body = template
                header(TYPESENSE_API_KEY_HEADER, TYPESENSE_API_KEY)
            }
        }
    }

}