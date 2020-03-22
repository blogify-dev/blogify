package blogify.backend.persistence.postgres.orm.models

import blogify.backend.resources.reflect.models.PropMap
import com.andreapivetta.kolor.red
import org.jetbrains.exposed.exceptions.DuplicateColumnException
import org.jetbrains.exposed.sql.CharacterColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.FloatColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType
import java.util.UUID
import kotlin.reflect.KClass

import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

sealed class PropertyMapping {

    abstract fun applyMappingToTable(table: Table)

    class ValueMapping(val handle: PropMap.PropertyHandle.Ok) : PropertyMapping() {

        lateinit var column: Column<*>

        override fun applyMappingToTable(table: Table) {
            val type = handle.property.returnType
            val typeClass = type.classifier as KClass<*>

            try {
                this.column = when {
                    type.isSubtypeOf(UUID::class.createType()) -> {
                        table.registerColumn<UUID>(handle.name, UUIDColumnType())
                    }
                    type.isSubtypeOf(String::class.createType()) -> {
                         table.registerColumn<String>(handle.name, TextColumnType())
                    }
                    type.isSubtypeOf(Int::class.createType()) -> {
                        table.registerColumn<Int>(handle.name, IntegerColumnType())
                    }
                    type.isSubtypeOf(Long::class.createType()) -> {
                        table.registerColumn<Long>(handle.name, LongColumnType())
                    }
                    type.isSubtypeOf(Double::class.createType()) -> {
                        table.registerColumn<Double>(handle.name, DoubleColumnType())
                    }
                    type.isSubtypeOf(Float::class.createType()) -> {
                        table.registerColumn<Float>(handle.name, FloatColumnType())
                    }
                    type.isSubtypeOf(Char::class.createType()) -> {
                        table.registerColumn<Char>(handle.name, CharacterColumnType())
                    }
                    else -> error("fatal: cannot generate a value mapping for a property of type '${typeClass.simpleName}'".red())
                }
            } catch (e: DuplicateColumnException) {
                error("fatal: duplicate column name (DuplicateColumnException thrown) when generating value mapping for property '${handle.name}' of class '${typeClass.simpleName}".red())
            }
        }

    }

    class AssociativeMapping : PropertyMapping() {

        enum class Cardinality {
            ONE_TO_ONE,
            ONE_TO_ONE_OR_NONE,
            MANY_TO_ONE,
            MANY_OR_NONE_TO_MANY_OR_NONE,
            ONE_TO_MANY,
            MANY_TO_MANY
        }

        override fun applyMappingToTable(table: Table) {
            TODO("Not yet implemented")
        }

    }

}
