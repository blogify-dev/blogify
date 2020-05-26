package blogify.backend.database.binding

import blogify.backend.database.extensions.foreignKeyTo
import blogify.backend.database.models.ResourceTable
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

import java.util.*

import kotlin.reflect.KProperty1

/**
 * Represents a binding between a [property] of [TResource]`::class and it's storage in the database
 *
 * @see Value
 * @see Reference
 * @see NullableReference
 * @see ReferenceToMany
 *
 * @author Benjozork
 *
 * @property table    the table for [TResource]
 * @property property the [property][KProperty1] we are binding
 */
sealed class SqlBinding<TResource : Resource, TProperty : Any?, TColumn : Any?> (
    val table: ResourceTable<TResource>,
    val property: KProperty1<TResource, TProperty>
) {

    /**
     * Applies the binding to an [update / insert builder][UpdateBuilder]
     */
    open fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {}

    interface HasColumn<TProperty : Any?> {
        val column: Column<TProperty>
    }

    /**
     * Binds [property] to a column of [table]
     */
    class Value<TResource : Resource, TProperty : Any?> (
        table: ResourceTable<TResource>,
        property: KProperty1<TResource, TProperty>,
        override val column: Column<TProperty>
    ) : SqlBinding<TResource, TProperty, TProperty>(table, property), HasColumn<TProperty> {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value
        }

    }

    /**
     * Binds [property] to a column of [table] containing UUIDs
     */
    class Reference<TResource : Resource, TProperty : Resource> (
        table: ResourceTable<TResource>,
        property: KProperty1<TResource, TProperty>,
        override val column: Column<UUID>
    ) : SqlBinding<TResource, TProperty, UUID>(table, property), HasColumn<UUID> {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value.uuid
        }

    }

    /**
     * Binds [property] to a nullable column of [table] containing UUIDs
     */
    class NullableReference<TResource : Resource, TProperty : Resource?> (
        table: ResourceTable<TResource>,
        property: KProperty1<TResource, TProperty>,
        override val column: Column<UUID?>
    ) : SqlBinding<TResource, TProperty, UUID?>(table, property), HasColumn<UUID?> {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value?.uuid
        }

    }

    /**
     * Binds [property] to [another table][otherTable] containing its values
     *
     * note: [otherTable] must contain one and only one FK from one if its columns with UUID type to the PK of [table]
     */
    class ReferenceToMany<TResource : Resource, TProperty : Any> (
        table: ResourceTable<TResource>,
        property: KProperty1<TResource, Collection<TProperty>>,
        val otherTable: Table,
        val conversionFunction: (ResultRow) -> TProperty,
        val insertionFunction: (TResource, TProperty, UpdateBuilder<Number>) -> Unit
    ) : SqlBinding<TResource, Collection<TProperty>, UUID>(table, property) {

        @Suppress("UNCHECKED_CAST")
        val otherTableFkToPkCol = otherTable.foreignKeyTo(table)
            ?: error("cannot make a ReferenceToMany SqlBinding: no foreign key from target table to PK of origin table")

    }

}
