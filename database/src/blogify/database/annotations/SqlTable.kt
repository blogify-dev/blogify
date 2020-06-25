package blogify.database.annotations

import reflectify.entity.Entity
import blogify.database.EntityTable

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Associates a [Entity] class to a [EntityTable]
 *
 * @param table the class of the table in which instances of this class are stored
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
annotation class SqlTable (
    val table: KClass<out EntityTable<*>>
)

/**
 * Computed value that finds the [EntityTable] for a given subclass of [Entity] using the [SqlTable] annotation
 */
@ExperimentalStdlibApi
@Suppress("UNCHECKED_CAST")
val <R : Entity> KClass<R>.table: EntityTable<R>
    get() {
        val annotation = this.findAnnotation<SqlTable>() ?: error("no @SqlTable annotation on class ${this.simpleName}")

        return (annotation.table.objectInstance ?: error("class referenced in @SqlTable annotation must be singleton"))
                as EntityTable<R>
    }
