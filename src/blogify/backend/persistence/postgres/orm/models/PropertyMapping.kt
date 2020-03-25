package blogify.backend.persistence.postgres.orm.models

import blogify.backend.persistence.postgres.orm.AssociativeTableGenerator
import blogify.backend.persistence.postgres.orm.extensions.isType
import blogify.backend.persistence.postgres.orm.extensions.subtypeOf
import blogify.backend.persistence.postgres.orm.annotations.Cardinality as CardinalityAnnotation
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
import kotlin.reflect.full.findAnnotation

import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.ReferenceOption

import com.andreapivetta.kolor.red
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.StringColumnType

sealed class PropertyMapping {

    abstract fun applyMappingToTable(table: OrmTable<*>): Column<*>?

    data class IdentifierMapping(val handle: PropMap.PropertyHandle.Ok<*>) : PropertyMapping() {

        override fun applyMappingToTable(table: OrmTable<*>): Column<UUID> {
            if (!(handle.property.returnType isType UUID::class))
                error("fatal: IdentifierMapping must be made on a UUID property".red())

            return table.registerColumn("uuid", UUIDColumnType())
        }

    }

    data class ValueMapping(val handle: PropMap.PropertyHandle.Ok<*>) : PropertyMapping() {

        lateinit var column: Column<*>

        override fun applyMappingToTable(table: OrmTable<*>): Column<*> {
            val type = handle.property.returnType
            val typeClass = type.classifier as KClass<*>

            try {
                this.column = when {
                    type isType UUID::class    -> table.registerColumn<UUID>(handle.name, UUIDColumnType())
                    type isType String::class  -> table.registerColumn<String>(handle.name, TextColumnType())
                    type isType Boolean::class -> table.registerColumn<Boolean>(handle.name, BooleanColumnType())
                    type isType Int::class     -> table.registerColumn<Int>(handle.name, IntegerColumnType())
                    type isType Long::class    -> table.registerColumn<Long>(handle.name, LongColumnType())
                    type isType Double::class  -> table.registerColumn<Double>(handle.name, DoubleColumnType())
                    type isType Float::class   -> table.registerColumn<Float>(handle.name, FloatColumnType())
                    type isType Char::class    -> table.registerColumn<Char>(handle.name, CharacterColumnType())
                    else -> error("fatal: cannot generate a value mapping for a property of type '${typeClass.simpleName}'".red())
                }
            } catch (e: DuplicateColumnException) {
                error("fatal: duplicate column name (DuplicateColumnException thrown) when generating value mapping for property '${handle.name}' of class '${typeClass.simpleName}".red())
            }

            return this.column
        }

    }

    data class AssociativeMapping<TLeftResource : Resource> (
        val leftHandle: PropMap.PropertyHandle.Ok<TLeftResource>
    ) : PropertyMapping() {

        var complete = false

        val cardinality = findCardinality(leftHandle)

        private val returnType = leftHandle.property.returnType

        @Suppress("UNCHECKED_CAST")
        val dependency =
            if (returnType subtypeOf Collection::class) {
                val collectionElementType = returnType.arguments.first().type
                    ?: error("fatal: found a star projection in property type's type parameter '${leftHandle.name}' of class '${leftHandle.klass.simpleName}'".red())

                if (collectionElementType subtypeOf Resource::class) {
                    collectionElementType.classifier as KClass<Resource>
                } else error("fatal: collection element type must be subtype of Resource (was '${(collectionElementType.classifier as KClass<*>).simpleName}')".red())
            } else returnType.classifier as KClass<Resource>

        lateinit var rightAssociationColumn: Column<UUID>
        lateinit var associationTable: Table

        fun complete(leftTable: OrmTable<in TLeftResource>, rightTable: OrmTable<in Resource>) {
            require(!complete) { "fatal: associative mapping is already completed".red() }

            when (cardinality) {
                Cardinality.MANY_TO_ONE,
                Cardinality.ONE_TO_ONE_OR_NONE -> // Take care of the first and second later
                    error("fatal: MANY_TO_ONE and ONE_TO_ONE_OR_NONE cardinalities are not supported yet".red())
                Cardinality.ONE_TO_ONE ->
                    this.rightAssociationColumn = rightTable.identifyingColumn
                Cardinality.ONE_TO_MANY,
                Cardinality.MANY_TO_MANY ->
                    this.associationTable = AssociativeTableGenerator.makeAssociativeTable(leftHandle, leftTable, rightTable, cardinality)
            }

            this.complete = true
        }

        override fun applyMappingToTable(table: OrmTable<*>): Column<*>? {
            require(complete) { "fatal: cannot apply associative mapping to table if it wasn't completed".red() }

            if (this::associationTable.isInitialized) {
                table.dependencyTables.add(associationTable)
            } else if (this::rightAssociationColumn.isInitialized) {
                val leftAssociationColumn = table.registerColumn<UUID>(leftHandle.name, UUIDColumnType())

                leftAssociationColumn.foreignKey = ForeignKeyConstraint (
                    rightAssociationColumn,
                    leftAssociationColumn,
                    ReferenceOption.RESTRICT,
                    ReferenceOption.RESTRICT,
                    name = "fk_${leftAssociationColumn.name}_${rightAssociationColumn.table.tableName}"
                )
            }

            return null
        }

    }

    data class PrimitiveAssociativeMapping<TLeftResource : Resource> (
        val leftHandle: PropMap.PropertyHandle.Ok<TLeftResource>
    ): PropertyMapping() {

        var complete = false

        lateinit var associationTable: Table

        @Suppress("UNCHECKED_CAST")
        fun complete(leftTable: OrmTable<in TLeftResource>) {
            require(!complete) { "fatal: associative mapping is already completed".red() }

            val type = leftHandle.property.returnType
            val typeClass = type.classifier as KClass<*>

            try {
                val collectionType = type.arguments.first().type
                    ?: error("fatal: found a star projection in property '${leftHandle.name}' of class '${leftHandle.klass.simpleName}'".red())
                val collectionTypeClass = collectionType.classifier as KClass<*>

                this.associationTable = AssociativeTableGenerator.makePrimitiveAssociativeTable (
                    leftHandle, leftTable as OrmTable<TLeftResource>,
                    rightColumnType = when {
                        collectionType isType UUID::class    -> UUIDColumnType()
                        collectionType isType String::class  -> TextColumnType()
                        collectionType isType Boolean::class -> BooleanColumnType()
                        collectionType isType Int::class     -> IntegerColumnType()
                        collectionType isType Long::class    -> LongColumnType()
                        collectionType isType Double::class  -> DoubleColumnType()
                        collectionType isType Float::class   -> FloatColumnType()
                        collectionType isType Char::class    -> CharacterColumnType()
                        else -> error("fatal: cannot generate a value mapping for a property of type '${collectionTypeClass.simpleName}'".red())
                    }
                )
            } catch (e: DuplicateColumnException) {
                error("fatal: duplicate column name (DuplicateColumnException thrown) when generating value mapping for property '${leftHandle.name}' of class '${typeClass.simpleName}".red())
            }

            this.complete = true
        }

        override fun applyMappingToTable(table: OrmTable<*>): Column<*>? {
            require(complete) { "fatal: cannot apply associative mapping to table if it wasn't completed".red() }

            table.dependencyTables.add(this.associationTable)

            return null
        }

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

                collectionElementType.type?.let {
                    it.findAnnotation<CardinalityAnnotation>()?.cardinality?.let { cardinality ->
                        when (cardinality) {
                            CollectionCardinality.ONE_TO_MANY  -> Cardinality.ONE_TO_MANY
                            CollectionCardinality.MANY_TO_MANY -> Cardinality.MANY_TO_MANY
                        }
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
