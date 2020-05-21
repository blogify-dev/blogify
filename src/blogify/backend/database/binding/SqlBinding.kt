package blogify.backend.database.binding

import blogify.backend.database.models.ResourceTable
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.UpdateBuilder

import java.util.*

import kotlin.reflect.KProperty1

sealed class SqlBinding<TResource : Resource, TProperty : Any?, TColumn : Any?> (
    val table: ResourceTable<TResource>,
    val column: Column<TColumn>,
    val property: KProperty1<TResource, TProperty>
) {

    abstract fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty)

    class Value<TResource : Resource, TProperty : Any?> (
        table: ResourceTable<TResource>,
        column: Column<TProperty>,
        property: KProperty1<TResource, TProperty>
    ) : SqlBinding<TResource, TProperty, TProperty>(table, column, property) {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value
        }

    }

    class Reference<TResource : Resource, TProperty : Resource> (
        table: ResourceTable<TResource>,
        column: Column<UUID>,
        property: KProperty1<TResource, TProperty>
    ) : SqlBinding<TResource, TProperty, UUID>(table, column, property) {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value.uuid
        }

    }

    class NullableReference<TResource : Resource, TProperty : Resource?> (
        table: ResourceTable<TResource>,
        column: Column<UUID?>,
        property: KProperty1<TResource, TProperty>
    ) : SqlBinding<TResource, TProperty, UUID?>(table, column, property) {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value?.uuid
        }

    }

}
