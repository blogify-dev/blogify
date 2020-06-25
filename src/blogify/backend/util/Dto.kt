package blogify.backend.util

import blogify.common.util.letCatchingOrNull
import blogify.common.util.Sr
import blogify.common.util.WrapBlocking
import reflectr.models.Mapped
import reflectr.models.PropMap
import reflectr.entity.mappedByHandles
import blogify.backend.appContext

import com.fasterxml.jackson.module.kotlin.readValue
import reflectr.util.Dto
import reflectr.util.MappedData

import kotlin.reflect.KClass

/**
 * Attempts to parse a string into a [Dto]
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
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
@ExperimentalStdlibApi
fun <TMapped : Mapped> String.parseJsonHandleMap(klass: KClass<TMapped>): Sr<MappedData> {
    return WrapBlocking { this.toDto()?.mappedByHandles(klass)!!.get() }
}
