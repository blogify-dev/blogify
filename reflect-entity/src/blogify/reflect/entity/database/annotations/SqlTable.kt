package blogify.reflect.entity.database.annotations

import blogify.reflect.entity.Entity
import blogify.reflect.entity.database.ResourceTable

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Associates a [Entity] class to a [ResourceTable]
 *
 * @param table the class of the table in which instances of this class are stored
 *
 * @author Benjozork
 */
annotation class SqlTable (
    val table: KClass<out ResourceTable<*>>
)

/**
 * Computed value that finds the [ResourceTable] for a given subclass of [Entity] using the [SqlTable] annotation
 */
@Suppress("UNCHECKED_CAST")
val <R : Entity> KClass<R>.table: ResourceTable<R>
    get() = (this.findAnnotation<SqlTable>()?.table?.objectInstance ?: error("no @SqlTable annotation on class ${this.simpleName}"))
            as ResourceTable<R>
