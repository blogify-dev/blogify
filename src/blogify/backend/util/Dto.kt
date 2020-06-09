package blogify.backend.util

import blogify.common.util.letCatchingOrNull
import blogify.reflect.Dto
import blogify.reflect.MappedData
import blogify.reflect.propMap
import blogify.reflect.unsafePropMap
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok
import blogify.backend.appContext
import blogify.common.util.Sr
import blogify.common.util.WrapBlocking

import com.fasterxml.jackson.module.kotlin.readValue

import kotlin.reflect.KClass

/**
 * Attempts to parse a string into a [Dto]
 *
 * @author Benjozork
 */
fun String.toDto(): Dto? =
    letCatchingOrNull {
        appContext.objectMapper.readValue<Dto>(it)
    }

/**
 * Attempts to turn a [Dto]'s String keys into [property handles][PropMap.PropertyHandle.Ok] of [klass]
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> Dto.mappedByHandles(klass: KClass<TMapped>, unsafe: Boolean = false): Sr<MappedData> {
    return WrapBlocking {
        this.map { (key, value) ->
            ((if (!unsafe) klass.propMap else klass.unsafePropMap)
                .ok.values
                .firstOrNull { it.name == key } ?: error("unknown key '$key'")) to value
        }.toMap()
    }
}

/**
 * Attempts to turn a JSON string into a map with its string keys turned into [property handles][PropMap.PropertyHandle.Ok] of [klass].
 *
 * The string must be valid JSON and the keys must be `Ok` property handles, otherwise the returned [Sr]
 * will be a failure.
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> String.parseJsonHandleMap(klass: KClass<TMapped>): Sr<MappedData> {
    return WrapBlocking { this.toDto()?.mappedByHandles(klass)!!.get() }
}
