package blogify.backend.database.extensions

import blogify.backend.annotations.table
import blogify.backend.database.binding.SqlBinding
import blogify.backend.resources.models.Resource
import blogify.backend.util.never
import blogify.reflect.extensions.klass

import org.jetbrains.exposed.sql.Column

import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
val <TResource : Resource, TProperty : Any?> KProperty1<TResource, TProperty>.binding: SqlBinding<TResource, TProperty, *>
    get() = this.klass.table.bindings.firstOrNull { it.property == this } as? SqlBinding<TResource, TProperty, *>
        ?: never

@Suppress("UNCHECKED_CAST")
val <TResource : Resource, TProperty : Any?> KProperty1<TResource, TProperty>.column: Column<*>
    get() = this.binding.column
