package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedUnsafePropMap
import blogify.backend.resources.reflect.models.ext.ok

import kotlin.reflect.KClass

import com.andreapivetta.kolor.red

object ClassMapper {

    fun mapClasses(vararg klasses: KClass<out Resource>, mappedClasses: MutableMap<KClass<*>, OrmTable<*>> = mutableMapOf()): Set<OrmTable<*>> {
         for (klass in klasses) {
             mappedClasses[klass] = mapSingleClass(klass)
         }

        return mappedClasses.values
            .map { it.also { resolveAssociativeMappings(it, mappedClasses) } }
            .toSet()
    }

    fun <TResource : Resource> mapSingleClass(klass: KClass<TResource>): OrmTable<TResource> {
        val propertyMappings = klass.cachedUnsafePropMap().ok()
            .map { PropertyMapper.mapProperty(it.value) }
            .toSet()

        val table = OrmTable(klass, propertyMappings)

        table.mappings.filter { it !is PropertyMapping.AssociativeTableMapping }
            .forEach { it.applyToTable(table) }

        table.mappings.filterIsInstance<PropertyMapping.IdentifierMapping>().firstOrNull()
            ?.column?.let { uuidColumn ->
                table.primaryKey = table.PrimaryKey(uuidColumn)
            } ?: error("fatal: table doesn't have exactly one IdentifierMapping".red())

        return table
    }

    @Suppress("UNCHECKED_CAST")
    fun <TResource : Resource> resolveAssociativeMappings(table: OrmTable<TResource>, mappedClasses: Map<KClass<*>, OrmTable<*>>) {
        table.remainingAssociativeMappings().forEach { mapping ->
            val dependency = mappedClasses.entries.find { it.key == mapping.dependency }?.value
                ?: error("fatal: no mapped table for dependency on '${mapping.dependency.simpleName}' of class '${mapping.handle.klass.simpleName}'")

            mapping.complete(table as OrmTable<in Resource>, dependency as OrmTable<in Resource>)
            mapping.applyToTable(table)
        }

        table.remainingPrimitiveAssociativeMappings().forEach { mapping ->
            mapping.complete(table as OrmTable<in Resource>)
            mapping.applyToTable(table)
        }
    }

}
