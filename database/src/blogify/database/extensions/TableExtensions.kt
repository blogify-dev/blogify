package blogify.database.extensions

import blogify.database.EntityTable
import reflectify.entity.Entity

import org.jetbrains.exposed.sql.*

import java.util.*

@ExperimentalStdlibApi
fun <TEntity : Entity> Table.weaKey(name: String, other: EntityTable<TEntity>): Column<UUID?> =
        registerColumn<UUID?>(name, UUIDColumnType().apply { nullable = true }) nullableKeyOf other

@ExperimentalStdlibApi
fun <TEntity : Entity> Table.strongKey(name: String, other: EntityTable<TEntity>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) keyOf other

@ExperimentalStdlibApi
fun <TEntity : Entity> Table.parentKey(name: String, other: EntityTable<TEntity>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) dependentKeyOf other
