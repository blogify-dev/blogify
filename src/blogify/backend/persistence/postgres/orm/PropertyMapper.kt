package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.extensions.isType
import blogify.backend.persistence.postgres.orm.extensions.subtypeOf
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap

import kotlin.reflect.KClass

import com.andreapivetta.kolor.red

import java.util.UUID

object PropertyMapper {

    fun mapProperty(klass: KClass<*>, handle: PropMap.PropertyHandle.Ok): PropertyMapping {

        val type = handle.property.returnType

        return when {
            type subtypeOf Number::class ||
            type isType    String::class ||
            type isType    Char::class   ||
            type isType    UUID::class     -> PropertyMapping.ValueMapping(handle)
            type subtypeOf Resource::class -> return AssociativePropertyMapper.mapAssociativeProperty(handle, klass)
            else -> error("fatal: I don't know how to map property '${handle.name}' of class '${klass.simpleName}'".red())
        }
    }

}
