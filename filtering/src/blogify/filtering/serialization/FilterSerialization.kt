package blogify.filtering.serialization

import blogify.filtering.Filter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object FilterSerialization {

    class FilterSerializer : StdSerializer<Filter<*, *>>(Filter::class.java) {

        override fun serialize(value: Filter<*, *>?, gen: JsonGenerator?, provider: SerializerProvider?) {
            TODO("Not yet implemented")
        }

    }

    class FilterDeserializer : StdDeserializer<Filter<*, *>>(Filter::class.java) {

        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Filter<*, *> {
            TODO("Not yet implemented")
        }

    }

}
