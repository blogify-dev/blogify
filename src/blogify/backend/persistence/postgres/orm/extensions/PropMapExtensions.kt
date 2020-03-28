package blogify.backend.persistence.postgres.orm.extensions

import blogify.backend.persistence.postgres.orm.ClassMapper
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.util.memoized

import kotlin.reflect.KClass

val <TResource : Resource> PropMap<TResource>.mappedTable: OrmTable<TResource>
    get() = { klass: KClass<TResource> ->
        ClassMapper.mapSingleClass(klass)
    }.memoized()(this.klass)

val <TResource : Resource> (PropMap.PropertyHandle<TResource>).mapping: PropertyMapping
    get() = { handle: PropMap.PropertyHandle<TResource> ->
        handle.klass.cachedPropMap().mappedTable.mappings.firstOrNull { it.handle == handle }
            ?: error("")
    }.memoized()(this)
