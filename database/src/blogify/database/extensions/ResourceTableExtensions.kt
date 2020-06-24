package blogify.database.extensions

import blogify.reflect.entity.Entity
import blogify.database.EntityTable

import blogify.reflect.extensions.klass

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

import java.util.*

/**
 * Returns the class of the type of [Entity] this table stores. Requires at least one [blogify.database.binding.SqlBinding] to be
 * declared.
 *
 * @author Benjozork
 */
val <TEntity : Entity> EntityTable<TEntity>.klass get() = this.bindings.first().property.klass

/**
 * Returns the column in the left side table with a foreign key to [other]'s `uuid` column, or null if none or multiple exist.
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
fun Table.foreignKeyTo(other: EntityTable<*>): Column<UUID>?
        = this.columns.singleOrNull { it.referee == other.uuid } as? Column<UUID>
