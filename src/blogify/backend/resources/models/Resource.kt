package blogify.backend.resources.models

import blogify.backend.entity.Resource
import blogify.common.util.never

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

/**
 * Used to serialize [resources][Resource] by only printing their [uuid][Resource.uuid].
 */
@ExperimentalStdlibApi
object ResourceIdSerializer : StdSerializer<Resource>(Resource::class.java) {

    override fun serialize(value: Resource, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.uuid.toString())
    }

}

