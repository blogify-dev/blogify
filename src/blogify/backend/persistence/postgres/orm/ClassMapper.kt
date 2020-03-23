package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedUnsafePropMap
import blogify.backend.resources.reflect.models.ext.ok

import org.jetbrains.exposed.sql.Table

import kotlin.reflect.KClass

object ClassMapper {

    fun <TResource : Resource> mapClasses(vararg klasses: KClass<TResource>): Set<Table> {
        return klasses.map { mapSingleClass(it) }.toSet()
    }

    private fun <TResource : Resource> mapSingleClass(klass: KClass<TResource>): Table {
        val propertyMappings = klass.cachedUnsafePropMap().ok()
            .map { PropertyMapper.mapProperty(klass, it.value) }

        return Table().also { table -> propertyMappings
            .filterIsInstance<PropertyMapping.ValueMapping>()
            .forEach { it.applyMappingToTable(table) }
        }
    }

}
