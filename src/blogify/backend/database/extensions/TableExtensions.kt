package blogify.backend.database.extensions

import blogify.backend.database.models.ResourceTable
import blogify.backend.entity.Resource

import org.jetbrains.exposed.sql.*

import java.util.*

fun <TResource : Resource> Table.weaKey(name: String, other: ResourceTable<TResource>): Column<UUID?> =
        registerColumn<UUID?>(name, UUIDColumnType().apply { nullable = true }) nullableKeyOf other

fun <TResource : Resource> Table.strongKey(name: String, other: ResourceTable<TResource>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) keyOf other

fun <TResource : Resource> Table.parentKey(name: String, other: ResourceTable<TResource>): Column<UUID> =
        registerColumn<UUID>(name, UUIDColumnType()) dependentKeyOf other
