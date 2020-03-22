package blogify.backend.persistence.postgres.orm

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.ok

import org.jetbrains.exposed.sql.Table

import kotlin.reflect.KClass

object ResourceMapper {

    fun <R : Resource> mapResourceClass(klass: KClass<R>): Table {
        val propertiesToMap = klass.cachedPropMap().ok()
        val mappings = propertiesToMap.map { PropertyMapper.mapProperty(klass, it.value) }

        return Table().also { table -> mappings.forEach { it.applyMappingToTable(table) } }
    }

}
