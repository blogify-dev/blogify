package blogify.backend.persistence.postgres.orm.extensions

import blogify.backend.persistence.postgres.orm.ClassMapper
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.util.letCatchingOrNull

import java.util.concurrent.ConcurrentHashMap

import kotlin.reflect.KClass

import com.andreapivetta.kolor.red

private val mappedClasses = ConcurrentHashMap<KClass<*>, OrmTable<*>>()

/**
 * Retrieves an [OrmTable] for the receiver class. Intensive operation, cached after first access.
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
val <TResource : Resource> KClass<TResource>.mappedTable: OrmTable<TResource>
    get() = mappedClasses.getOrPut(this) { ClassMapper.mapClasses(this, mappedClasses = mappedClasses).first { it.klass == this } } as OrmTable<TResource>

/**
 * Retrieves an [OrmTable] for the receiver class. Not cached. Requires an [OrmTable] to have already been mapped by [mappedTable].
 *
 * @author Benjozork
 */
val <TResource : Resource> PropMap.PropertyHandle<TResource>.mapping
    get() = letCatchingOrNull { this.klass.mappedTable }?.let {
        it.mappings.firstOrNull { m -> m.handle == this }
                ?: error("fatal: could not find mapping for property '${this.name}' of class '${this.klass.simpleName}'".red())
    } ?: error("fatal: could not get mapped table for class '${this.klass.simpleName}'")
