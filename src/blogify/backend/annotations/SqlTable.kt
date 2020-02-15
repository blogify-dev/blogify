package blogify.backend.annotations

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

annotation class SqlTable (
    val table: KClass<out ResourceTable<*>>
)

val <R : Resource> KClass<R>.table: ResourceTable<R>
    get() = (this.findAnnotation<SqlTable>()?.table?.objectInstance ?: error("no @SqlTable annotation on class ${this.simpleName}"))
            as ResourceTable<R>
