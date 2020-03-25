package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.extensions.isPrimitive
import blogify.backend.persistence.postgres.orm.extensions.isResource
import blogify.backend.persistence.postgres.orm.extensions.isType
import blogify.backend.persistence.postgres.orm.extensions.subtypeOf
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap

import com.andreapivetta.kolor.red

import java.util.UUID

object PropertyMapper {

    @Suppress("UNCHECKED_CAST")
    fun <TResource : Resource> mapProperty(handle: PropMap.PropertyHandle.Ok<TResource>): PropertyMapping {
        val type = handle.property.returnType
        val klass = handle.klass

        return when {
            type subtypeOf Number::class  ||
            type isType    String::class  ||
            type isType    Boolean::class ||
            type isType    Char::class -> PropertyMapping.ValueMapping(handle)
            type isType    UUID::class -> PropertyMapping.IdentifierMapping(handle)
            type subtypeOf Collection::class -> {
                val collectionType = type.arguments.first().type ?: error("fatal: found a star projection in property type's type parameter '${handle.name}' of class '${handle.klass.simpleName}'".red())
                when {
                    collectionType.isResource()  -> PropertyMapping.AssociativeMapping(handle)
                    collectionType.isPrimitive() -> PropertyMapping.PrimitiveAssociativeMapping(handle)
                    else -> error("fatal: I don't know how to map collection property type '${handle.name}' of class '${klass.simpleName}'".red())
                }
            }
            type subtypeOf Resource::class -> PropertyMapping.AssociativeMapping(handle)
            else -> error("fatal: I don't know how to map property '${handle.name}' of class '${klass.simpleName}'".red())
        }
    }

}
