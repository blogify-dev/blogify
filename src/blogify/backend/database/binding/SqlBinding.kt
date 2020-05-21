package blogify.backend.database.binding

import blogify.backend.database.models.ResourceTable
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

import java.util.*

import kotlin.reflect.KProperty1

sealed class SqlBinding<TResource : Resource, TProperty : Any?, TColumn : Any?> (
    val table: ResourceTable<TResource>,
    val column: Column<TColumn>?,
    val property: KProperty1<TResource, TProperty>
) {

    open fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {}

    open val selectSubQuery: (UUID) -> Query = { TODO() }

    class Value<TResource : Resource, TProperty : Any?> (
        table: ResourceTable<TResource>,
        column: Column<TProperty>,
        property: KProperty1<TResource, TProperty>
    ) : SqlBinding<TResource, TProperty, TProperty>(table, column, property) {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column!!] = value
        }

    }

    class Reference<TResource : Resource, TProperty : Resource> (
        table: ResourceTable<TResource>,
        column: Column<UUID>,
        property: KProperty1<TResource, TProperty>
    ) : SqlBinding<TResource, TProperty, UUID>(table, column, property) {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column!!] = value.uuid
        }

    }

    class ReferenceToMany<TResource : Resource, TProperty : Any> (
        table: ResourceTable<TResource>,
        property: KProperty1<TResource, Collection<TProperty>>,
        val otherTable: Table,
        val conversionFunction: (ResultRow) -> TProperty,
        val insertionFunction: (TResource, TProperty, UpdateBuilder<Number>) -> Unit
    ) : SqlBinding<TResource, Collection<TProperty>, UUID>(table, null, property) {

        @Suppress("UNCHECKED_CAST")
        private val otherTableFkToPkCol = otherTable.columns.firstOrNull { it.referee == table.uuid } as? Column<UUID>
            ?: error("cannot make a ReferenceToMany SqlBinding: no foreign key from target table to PK of origin table")

        override val selectSubQuery: (UUID) -> Query =
            { uuid -> otherTable.select { otherTableFkToPkCol eq uuid } }

    }

    class NullableReference<TResource : Resource, TProperty : Resource?> (
        table: ResourceTable<TResource>,
        column: Column<UUID?>,
        property: KProperty1<TResource, TProperty>
    ) : SqlBinding<TResource, TProperty, UUID?>(table, column, property) {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column!!] = value?.uuid
        }

    }

}
