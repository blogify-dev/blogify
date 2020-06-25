package blogify.database.extensions

import blogify.database.EntityTable
import reflectr.entity.Entity

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.ReferenceOption

import java.util.*

@ExperimentalStdlibApi
infix fun <T : Entity, C : Column<UUID?>> C.nullableKeyOf(entityTable: EntityTable<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = entityTable.uuid,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.SET_NULL,
        name = null
    )
}

@ExperimentalStdlibApi
infix fun <T : Entity, C : Column<UUID>> C.keyOf(entityTable: EntityTable<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = entityTable.uuid,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.RESTRICT,
        name = null
    )}

@ExperimentalStdlibApi
infix fun <T : Entity, C : Column<UUID>> C.dependentKeyOf(entityTable: EntityTable<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = entityTable.uuid,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.CASCADE,
        name = null
    )}

infix fun <T : Comparable<T>, S : T, C : Column<S>> C.keyFrom(other: Column<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = other,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.CASCADE,
        name = null
    )}

infix fun <T : Comparable<T>, S : T, C : Column<S?>> C.weakKeyFrom(other: Column<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = other,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.SET_NULL,
        name = null
    )}
