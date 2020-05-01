package blogify.backend.util

import blogify.backend.appContext
import blogify.reflect.propMap
import blogify.reflect.unsafePropMap
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok

import com.fasterxml.jackson.module.kotlin.readValue

import kotlin.reflect.KClass

typealias Dto = Map<String, Any?>

/**
 * Attempts to parse a string into a [Dto]
 *
 * @author Benjozork
 */
fun String.toDto(): Dto? =
    letCatchingOrNull {
        return@letCatchingOrNull try {
            appContext.objectMapper.readValue<Dto>(it)
        } catch (e: Exception) {
            null
        }
    }

/**
 * Attempts to turn a [Dto]'s String keys into [property handles][PropMap.PropertyHandle.Ok] of [klass]
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> Dto.mappedByHandles(klass: KClass<TMapped>, unsafe: Boolean = false): Sr<Map<PropMap.PropertyHandle.Ok, Any?>> {
    return WrapBlocking { this.map { (key, value) ->
        ((if (!unsafe) klass.propMap else klass.unsafePropMap())
            .ok.values
            .firstOrNull { it.name == key } ?: error("unknown key '$key'")) to value
    }.toMap() }
}

/**
 * Attempts to turn a JSON string into a map with its string keys turned into [property handles][PropMap.PropertyHandle.Ok] of [klass].
 *
 * The string must be valid JSON and the keys must be `Ok` property handles, otherwise the returned [Sr]
 * will be a failure.
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> String.parseJsonHandleMap(klass: KClass<TMapped>): Sr<Map<PropMap.PropertyHandle.Ok, Any?>> {
    return WrapBlocking { this.toDto()?.mappedByHandles(klass)!!.get() }
}
