package blogify.backend.database.extensions

import blogify.backend.database.models.ResourceTable
import blogify.reflect.entity.Entity

import org.jetbrains.exposed.sql.*

import java.util.*

fun <TResource : Entity> Table.weaKey(name: String, other: ResourceTable<TResource>): Column<UUID?> =
        registerColumn<UUID?>(name, UUIDColumnType().apply { nullable = true }) nullableKeyOf other

fun <TResource : Entity> Table.strongKey(name: String, other: ResourceTable<TResource>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) keyOf other

fun <TResource : Entity> Table.parentKey(name: String, other: ResourceTable<TResource>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) dependentKeyOf other
