package blogify.backend.persistence.postgres.orm.models

import blogify.backend.persistence.postgres.orm.extensions.subtypeOf
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap

import org.jetbrains.exposed.sql.CharacterColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.FloatColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.exceptions.DuplicateColumnException

import java.util.UUID

import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

import com.andreapivetta.kolor.red
import blogify.backend.persistence.postgres.orm.annotations.Cardinality as CardinalityAnnotation

sealed class PropertyMapping {

    abstract fun applyMappingToTable(table: Table)

    data class ValueMapping(val handle: PropMap.PropertyHandle.Ok<*>) : PropertyMapping() {

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

    data class AssociativeMapping<TLeftResource : Resource> (
        val leftHandle: PropMap.PropertyHandle.Ok<TLeftResource>
    ) : PropertyMapping() {

        val cardinality = findCardinality(leftHandle)

        enum class Cardinality {
            ONE_TO_ONE,
            ONE_TO_ONE_OR_NONE,
            MANY_TO_ONE,
            ONE_TO_MANY,
            MANY_TO_MANY
        }

        override fun applyMappingToTable(table: Table) {
            TODO("Not yet implemented")
        }

        companion object {
            /**
             * Finds the cardinality of an associative mapping originating from [leftHandle]
             *
             * @param leftHandle the [PropMap.PropertyHandle.Ok] that is an associative mapping
             *
             * @return the resolved cardinality of the mapping
             *
             * @author Benjozork
             */
            fun findCardinality(leftHandle: PropMap.PropertyHandle.Ok<*>): Cardinality {
                val type = leftHandle.property.returnType
                val isCollectionType = type subtypeOf Collection::class

                return if (isCollectionType) {
                    val collectionElementType = type.arguments[0]

                    collectionElementType.type
                        ?.let { it.annotations.firstOrNull { a -> a is CardinalityAnnotation }
                            ?.let { annotation ->
                                (annotation as CardinalityAnnotation).cardinality
                            } ?: error("fatal: no cardinality annotation on collection element type for property '${leftHandle.name}' of class '${leftHandle.klass.simpleName}'".red())
                        } ?: error("fatal: found a star projection in property '${leftHandle.name}' of class '${leftHandle.klass.simpleName}'".red())
                } else {
                    if (type.isMarkedNullable)
                        Cardinality.ONE_TO_ONE_OR_NONE
                    else
                        Cardinality.ONE_TO_ONE
                }
            }
        }

    }

}
