package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedUnsafePropMap
import blogify.backend.resources.reflect.models.ext.ok

import kotlin.reflect.KClass

object ClassMapper {

    val mappedClasses = mutableMapOf<KClass<*>, OrmTable<*>>()

    fun mapClasses(vararg klasses: KClass<out Resource>): Set<OrmTable<*>> {
         for (klass in klasses) {
             mappedClasses[klass] = mapSingleClass(klass)
         }

        return mappedClasses.values
            .map { it.also { resolveAssociativeMappings(it) } }
            .toSet()
    }

    fun <TResource : Resource> mapSingleClass(klass: KClass<TResource>): OrmTable<TResource> {
        val propertyMappings = klass.cachedUnsafePropMap().ok()
            .map { PropertyMapper.mapProperty(klass, it.value) }
            .toSet()

        return OrmTable(klass, propertyMappings)
    }

    fun <TResource : Resource> resolveAssociativeMappings(table: OrmTable<TResource>) {
        val dependencies = table.remainingAssociativeMappings().associateWith { it.dependency }
        val mappedDependencies = dependencies.entries.associateWith { (depMapping, depClass) ->
            mappedClasses.entries.find { it.key == depClass }?.value
                ?: error("fatal: no mapped table for dependency on '${depClass.simpleName}' of class '${depMapping.leftHandle.klass.simpleName}'")
        }.mapKeys { it.key.key }

        @Suppress("UNCHECKED_CAST")
        mappedDependencies.forEach { (mapping, dependencytable) ->
            mapping.complete(table as OrmTable<in Resource>, dependencytable as OrmTable<in Resource>)
            mapping.applyMappingToTable(table)
        }
    }

}
