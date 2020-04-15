package blogify.backend.util

import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.cachedUnsafePropMap
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.ok

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import kotlin.reflect.KClass

typealias Dto = Map<String, Any?>

// This is thread safe, this is ok
private val dtoObjectMapper = jacksonObjectMapper()

/**
 * Parses a string into a [Dto]
 *
 * @author Benjozork
 */
fun String.toDto(): Dto? =
    letCatchingOrNull {
        return@letCatchingOrNull try {
            dtoObjectMapper.readValue(it, Map::class.java) as? Dto
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

fun <TMapped : Mapped> Dto.mappedByHandles(klass: KClass<TMapped>, unsafe: Boolean = false): Sr<Map<PropMap.PropertyHandle.Ok, Any?>> {
    return WrapBlocking { this.map { (key, value) ->
        ((if (!unsafe) klass.cachedPropMap() else klass.cachedUnsafePropMap())
            .ok().values
            .firstOrNull { it.name == key } ?: error("unknown key '$key'")) to value
    }.toMap() }
}
