package blogify.backend.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import io.ktor.http.ContentType

object ContentTypeSerializer : StdSerializer<ContentType>(ContentType::class.java) {

    override fun serialize(value: ContentType?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeString(value.toString())
    }

}

object ContentTypeDeserializer : StdDeserializer<ContentType>(ContentType::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ContentType {
        return ContentType.parse(p?.readValueAsTree<TextNode>()?.textValue() ?: never)
    }

}
