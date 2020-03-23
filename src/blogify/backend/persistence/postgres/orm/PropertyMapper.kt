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

    @Suppress("UNCHECKED_CAST")
    fun <TResource : Resource> findDependencyForHandle(handle: PropMap.PropertyHandle.Ok<TResource>): KClass<Resource>? {
        val type = handle.property.returnType

        return if (type subtypeOf Resource::class)
            type.classifier as KClass<Resource>
        else null
    }

    @Suppress("UNCHECKED_CAST")
    fun <TResource : Resource> mapProperty(klass: KClass<TResource>, handle: PropMap.PropertyHandle.Ok<TResource>): PropertyMapping {
        val type = handle.property.returnType

        return when {
            type subtypeOf Number::class ||
            type isType    String::class ||
            type isType    Char::class   ||
            type isType    UUID::class     -> PropertyMapping.ValueMapping(handle)
            type subtypeOf Resource::class -> PropertyMapping.AssociativeMapping(handle)
            else -> error("fatal: I don't know how to map property '${handle.name}' of class '${klass.simpleName}'".red())
        }
    }

}
