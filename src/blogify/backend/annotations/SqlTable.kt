package blogify.backend.annotations

import blogify.backend.database.models.ResourceTable
import blogify.backend.resources.models.Resource

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Associates a [Resource] class to a [ResourceTable] when using a [blogify.backend.persistence.PostgresDataStore]
 *
 * @param table the class of the table in which instances of this class are stored
 *
 * @author Benjozork
 */
annotation class SqlTable (
    val table: KClass<out ResourceTable<*>>
)

/**
 * Computed value that finds the [ResourceTable] for a given subclass of [Resource] using the [SqlTable] annotation
 */
val <R : Resource> KClass<R>.table: ResourceTable<R>
    get() = (this.findAnnotation<SqlTable>()?.table?.objectInstance ?: error("no @SqlTable annotation on class ${this.simpleName}"))
            as ResourceTable<R>
