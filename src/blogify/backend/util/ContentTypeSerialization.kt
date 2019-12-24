package blogify.backend.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import io.ktor.http.ContentType

object ContentTypeSerializer : StdSerializer<ContentType>(ContentType::class.java) {

    override fun serialize(value: ContentType?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeString(value.toString())
    }

}
