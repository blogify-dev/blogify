package blogify.backend.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.Instant

object InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
    override fun serialize(value: Instant?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.epochSecond?.toInt()?.let { gen?.writeNumber(it) } ?: error("wtf")
    }
}