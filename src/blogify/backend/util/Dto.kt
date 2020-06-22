package blogify.backend.util

import blogify.common.util.letCatchingOrNull
import blogify.common.util.Sr
import blogify.common.util.WrapBlocking
import blogify.reflect.*
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.entity.mappedByHandles
import blogify.backend.appContext

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
