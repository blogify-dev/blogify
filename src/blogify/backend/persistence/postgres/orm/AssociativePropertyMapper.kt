package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.annotations.Cardinality
import blogify.backend.persistence.postgres.orm.extensions.subtypeOf
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.persistence.postgres.orm.models.PropertyMapping.AssociativeMapping.Cardinality as AssociationCardinality
import blogify.backend.resources.reflect.models.PropMap

import com.andreapivetta.kolor.red

import kotlin.reflect.KClass

object AssociativePropertyMapper {

    fun mapAssociativeProperty(handle: PropMap.PropertyHandle.Ok, klass: KClass<*>): PropertyMapping {
        val cardinality = findCardinality(handle, klass)

        println(cardinality)

        TODO("Not yet implemented")
    }

    fun findCardinality(handle: PropMap.PropertyHandle.Ok, klass: KClass<*>): AssociationCardinality {
        val type = handle.property.returnType
        val isCollectionType = type subtypeOf Collection::class

        return if (isCollectionType) {
            val collectionElementType = type.arguments[0]

            collectionElementType.type
                ?.let { it.annotations.firstOrNull { a -> a is Cardinality }
                    ?.let { annotation ->
                        (annotation as Cardinality).cardinality
                    } ?: error("fatal: no cardinality annotation on collection element type for property '${handle.name}' of class '${klass.simpleName}'".red())
                } ?: error("fatal: found a star projection in property '${handle.name}' of class '${klass.simpleName}'".red())
        } else {
            if (type.isMarkedNullable)
                AssociationCardinality.ONE_TO_ONE_OR_NONE
            else
                AssociationCardinality.ONE_TO_ONE
        }
    }

}
