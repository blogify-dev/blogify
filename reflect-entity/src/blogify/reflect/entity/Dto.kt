package blogify.reflect.entity

import blogify.common.util.Sr
import blogify.common.util.WrapBlocking
import blogify.reflect.Dto
import blogify.reflect.MappedData
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok
import blogify.reflect.propMap
import blogify.reflect.unsafePropMap

import kotlin.reflect.KClass

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
                .firstOrNull { it.name == key } ?: kotlin.error("unknown key '$key'")) to value
        }.toMap()
    }
}
