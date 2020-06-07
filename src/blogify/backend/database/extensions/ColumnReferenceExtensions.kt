package blogify.backend.database.extensions

import blogify.backend.database.models.ResourceTable
import blogify.backend.entity.Resource

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.ReferenceOption

import java.util.*

infix fun <T : Resource, C : Column<UUID?>> C.nullableKeyOf(resourceTable: ResourceTable<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = resourceTable.uuid,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.SET_NULL,
        name = null
    )
}

infix fun <T : Resource, C : Column<UUID>> C.keyOf(resourceTable: ResourceTable<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = resourceTable.uuid,
        from = this,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.RESTRICT,
        name = null
    )}

infix fun <T : Resource, C : Column<UUID>> C.dependentKeyOf(resourceTable: ResourceTable<T>): C = apply {
    this.foreignKey = ForeignKeyConstraint (
        target = resourceTable.uuid,
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
