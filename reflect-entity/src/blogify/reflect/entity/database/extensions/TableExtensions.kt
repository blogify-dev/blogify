package blogify.reflect.entity.database.extensions

import blogify.reflect.entity.database.EntityTable
import blogify.reflect.entity.Entity

import org.jetbrains.exposed.sql.*

import java.util.*

fun <TEntity : Entity> Table.weaKey(name: String, other: EntityTable<TEntity>): Column<UUID?> =
        registerColumn<UUID?>(name, UUIDColumnType().apply { nullable = true }) nullableKeyOf other

fun <TEntity : Entity> Table.strongKey(name: String, other: EntityTable<TEntity>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) keyOf other

fun <TEntity : Entity> Table.parentKey(name: String, other: EntityTable<TEntity>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) dependentKeyOf other
