package blogify.database.binding

import reflectify.entity.Entity
import blogify.database.extensions.foreignKeyTo
import blogify.database.EntityTable

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

import java.util.*

import kotlin.reflect.KProperty1

/**
 * Represents a binding between a [property] of [TEntity]`::class and it's storage in the database
 *
 * @see Value
 * @see Reference
 * @see NullableReference
 * @see ReferenceToMany
 *
 * @author Benjozork
 *
 * @property table    the table for [TEntity]
 * @property property the [property][KProperty1] we are binding
 */
@ExperimentalStdlibApi
sealed class SqlBinding<TEntity : Entity, TProperty : Any?, TColumn : Any?> (
    val table: EntityTable<TEntity>,
    val property: KProperty1<TEntity, TProperty>
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
    class Value<TEntity : Entity, TProperty : Any?> (
        table: EntityTable<TEntity>,
        property: KProperty1<TEntity, TProperty>,
        override val column: Column<TProperty>
    ) : SqlBinding<TEntity, TProperty, TProperty>(table, property),
        HasColumn<TProperty> {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value
        }

    }

    /**
     * Binds [property] to a column of [table] containing UUIDs
     */
    class Reference<TEntity : Entity, TProperty : Entity> (
        table: EntityTable<TEntity>,
        property: KProperty1<TEntity, TProperty>,
        override val column: Column<UUID>
    ) : SqlBinding<TEntity, TProperty, UUID>(table, property),
        HasColumn<UUID> {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value.uuid
        }

    }

    /**
     * Binds [property] to a nullable column of [table] containing UUIDs
     */
    class NullableReference<TEntity : Entity, TProperty : Entity?> (
        table: EntityTable<TEntity>,
        property: KProperty1<TEntity, TProperty>,
        override val column: Column<UUID?>
    ) : SqlBinding<TEntity, TProperty, UUID?>(table, property),
        HasColumn<UUID?> {

        override fun applyToUpdateOrInsert(builder: UpdateBuilder<Number>, value: TProperty) {
            builder[column] = value?.uuid
        }

    }

    /**
     * Binds [property] to [another table][otherTable] containing its values
     *
     * note: [otherTable] must contain one and only one FK from one if its columns with UUID type to the PK of [table]
     */
    class ReferenceToMany<TEntity : Entity, TProperty : Any> (
        table: EntityTable<TEntity>,
        property: KProperty1<TEntity, Collection<TProperty>>,
        val otherTable: Table,
        val conversionFunction: (ResultRow) -> TProperty,
        val insertionFunction: (TEntity, TProperty, UpdateBuilder<Number>) -> Unit
    ) : SqlBinding<TEntity, Collection<TProperty>, UUID>(table, property) {

        @Suppress("UNCHECKED_CAST")
        val otherTableFkToPkCol = otherTable.foreignKeyTo(table)
            ?: error("cannot make a ReferenceToMany SqlBinding: no foreign key from target table to PK of origin table")

    }

}
