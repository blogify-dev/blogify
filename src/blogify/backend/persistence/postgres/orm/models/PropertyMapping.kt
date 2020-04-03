package blogify.backend.persistence.postgres.orm.models

import blogify.backend.persistence.postgres.orm.AssociativeTableGenerator
import blogify.backend.persistence.postgres.orm.extensions.isType
import blogify.backend.persistence.postgres.orm.extensions.klass
import blogify.backend.persistence.postgres.orm.extensions.subtypeOf
import blogify.backend.persistence.postgres.orm.annotations.Cardinality as CardinalityAnnotation
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap

import org.jetbrains.exposed.exceptions.DuplicateColumnException
import org.jetbrains.exposed.sql.*

import java.util.UUID

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

import com.andreapivetta.kolor.red

/**
 * Used to transform a class [property][kotlin.reflect.KProperty] into its SQL representation.
 *
 * @author Benjozork
 *
 * @property handle the [PropMap.PropertyHandle.Ok] of the property the mapping is used for
 */
sealed class PropertyMapping(open val handle: PropMap.PropertyHandle.Ok<*>) {

    /**
     * Applies the mapping to a created [OrmTable]. Requires the `complete` function to be called first
     * in [AssociativeMapping] and [PrimitiveAssociativeMapping].
     */
    abstract fun applyToTable(table: OrmTable<*>): Column<*>?

    abstract class AssociativeTableMapping(override val handle: PropMap.PropertyHandle.Ok<*>): PropertyMapping(handle) {
        abstract fun joinWith(join: ColumnSet): Join
    }

    /**
     * Maps the [Resource.uuid] property into an SQL primary key
     */
    data class IdentifierMapping(override val handle: PropMap.PropertyHandle.Ok<*>) : PropertyMapping(handle) {

        lateinit var column: Column<UUID>

        override fun applyToTable(table: OrmTable<*>): Column<UUID> {
            if (!(handle.property.returnType isType UUID::class))
                error("fatal: IdentifierMapping must be made on a UUID property".red())

            column = table.registerColumn("uuid", UUIDColumnType())
            return column
        }

    }

    /**
     * Maps a property with a primitive ([String], [Number], [Char]...) type to a single SQL column
     */
    data class ValueMapping(override val handle: PropMap.PropertyHandle.Ok<*>) : PropertyMapping(handle) {

        lateinit var column: Column<*>

        override fun applyToTable(table: OrmTable<*>): Column<*> {
            val type = handle.property.returnType
            val typeClass = type.classifier as KClass<*>
            val typeNullable = type.isMarkedNullable

            fun <TColumn> makeColumn(table: OrmTable<*>, name: String, nullable: Boolean, type: IColumnType): Column<*> =
                    table.registerColumn<TColumn>(name, type.also { it.nullable = nullable })

            try {
                this.column = when {
                    type isType UUID::class    -> makeColumn<UUID>    (table, handle.name, typeNullable, UUIDColumnType())
                    type isType String::class  -> makeColumn<String>  (table, handle.name, typeNullable, TextColumnType())
                    type isType Boolean::class -> makeColumn<Boolean> (table, handle.name, typeNullable, BooleanColumnType())
                    type isType Int::class     -> makeColumn<Int>     (table, handle.name, typeNullable, IntegerColumnType())
                    type isType Long::class    -> makeColumn<Long>    (table, handle.name, typeNullable, LongColumnType())
                    type isType Double::class  -> makeColumn<Double>  (table, handle.name, typeNullable, DoubleColumnType())
                    type isType Float::class   -> makeColumn<Float>   (table, handle.name, typeNullable, FloatColumnType())
                    type isType Char::class    -> makeColumn<Char>    (table, handle.name, typeNullable, CharacterColumnType())
                    else -> error("fatal: cannot generate a value mapping for a property of type '${typeClass.simpleName}'".red())
                }
            } catch (e: DuplicateColumnException) {
                error("fatal: duplicate column name (DuplicateColumnException thrown) when generating value mapping for property '${handle.name}' of class '${typeClass.simpleName}".red())
            }

            return this.column
        }

        operator fun invoke() = column

    }

    /**
     * Maps a property with a [Resource] type to an SQL relation. Only relations to `R | R?` and `C<R>` (where `C <: Collection`
     * and `R <: Resource`) are supported. Neither `C<R>?`or `C<R?>` are supported.
     */
    data class AssociativeMapping<TLeftResource : Resource> (
        override val handle: PropMap.PropertyHandle.Ok<TLeftResource>
    ) : AssociativeTableMapping(handle) {

        var complete = false

        val cardinality = findCardinality(handle)

        private val returnType = handle.property.returnType

        @Suppress("UNCHECKED_CAST")
        val dependency =
            if (returnType subtypeOf Collection::class) {
                require(!returnType.isMarkedNullable) { "fatal: collection property types cannot be marked nullable".red() }

                val collectionElementType = returnType.arguments.first().type
                    ?: error("fatal: found a star projection in property type's type parameter '${handle.name}' of class '${handle.klass.simpleName}'".red())

                require(!collectionElementType.isMarkedNullable) { "fatal: collection property element types cannot be marked nullable".red() }

                if (collectionElementType subtypeOf Resource::class) {
                    collectionElementType.klass<Resource>()
                } else error("fatal: collection element type must be subtype of Resource (was '${collectionElementType.klass<Any>().simpleName}')".red())
            } else returnType.classifier as KClass<Resource>

        lateinit var rightAssociationColumn: Column<UUID>
        lateinit var associationTable: Table

        fun complete(leftTable: OrmTable<in TLeftResource>, rightTable: OrmTable<in Resource>) {
            require(!complete) { "fatal: associative mapping is already completed".red() }

            when (cardinality) {
                Cardinality.MANY_TO_ONE ->
                    error("fatal: MANY_TO_ONE and ONE_TO_ONE_OR_NONE cardinalities are not supported yet".red())
                Cardinality.ONE_TO_ONE_OR_NONE,
                Cardinality.ONE_TO_ONE ->
                    this.rightAssociationColumn = rightTable.identifyingColumn
                Cardinality.ONE_TO_MANY,
                Cardinality.MANY_TO_MANY ->
                    this.associationTable = AssociativeTableGenerator.makeAssociativeTable(handle, leftTable, rightTable, cardinality)
            }

            this.complete = true
        }

        override fun applyToTable(table: OrmTable<*>): Column<*>? {
            require(complete) { "fatal: cannot apply associative mapping to table if it wasn't completed".red() }

            if (this::associationTable.isInitialized) {
                table.dependencyTables.add(associationTable)
            } else if (this::rightAssociationColumn.isInitialized) {
                val leftAssociationColumn = table.registerColumn<UUID> (
                    handle.name, UUIDColumnType().also { if (cardinality == Cardinality.ONE_TO_ONE_OR_NONE) it.nullable = true }
                )

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

        @Suppress("UNCHECKED_CAST")
        override fun joinWith(join: ColumnSet): Join {
            require(complete) { "fatal: associative mapping must be completed before joining".red() }

            return if (::associationTable.isInitialized) {
                join.leftJoin(associationTable).leftJoin(associationTable.columns[1].foreignKey!!.target.table)
            } else {
                join.leftJoin(rightAssociationColumn.table)
            }
        }

    }

    /**
     * Same as [AssociativeMapping], but for primitive values as discussed above.
     */
    data class PrimitiveAssociativeMapping<TLeftResource : Resource> (
        override val handle: PropMap.PropertyHandle.Ok<TLeftResource>
    ): AssociativeTableMapping(handle) {

        var complete = false

        lateinit var associationTable: Table

        @Suppress("UNCHECKED_CAST")
        fun complete(leftTable: OrmTable<in TLeftResource>) {
            require(!complete) { "fatal: associative mapping is already completed".red() }

            val type = handle.property.returnType
            val typeClass = type.classifier as KClass<*>

            require(!type.isMarkedNullable) { "fatal: collection property types cannot be marked nullable".red() }

            try {
                val collectionElementType = type.arguments.first().type
                    ?: error("fatal: found a star projection in property '${handle.name}' of class '${handle.klass.simpleName}'".red())
                val collectionTypeClass = collectionElementType.classifier as KClass<*>

                require(!collectionElementType.isMarkedNullable) { "fatal: collection property element types cannot be marked nullable".red() }

                this.associationTable = AssociativeTableGenerator.makePrimitiveAssociativeTable (
                    handle, leftTable as OrmTable<TLeftResource>,
                    rightColumnType = when {
                        collectionElementType isType UUID::class    -> UUIDColumnType()
                        collectionElementType isType String::class  -> TextColumnType()
                        collectionElementType isType Boolean::class -> BooleanColumnType()
                        collectionElementType isType Int::class     -> IntegerColumnType()
                        collectionElementType isType Long::class    -> LongColumnType()
                        collectionElementType isType Double::class  -> DoubleColumnType()
                        collectionElementType isType Float::class   -> FloatColumnType()
                        collectionElementType isType Char::class    -> CharacterColumnType()
                        else -> error("fatal: cannot generate a value mapping for a property of type '${collectionTypeClass.simpleName}'".red())
                    }
                )
            } catch (e: DuplicateColumnException) {
                error("fatal: duplicate column name (DuplicateColumnException thrown) when generating value mapping for property '${handle.name}' of class '${typeClass.simpleName}".red())
            }

            this.complete = true
        }

        override fun applyToTable(table: OrmTable<*>): Column<*>? {
            require(complete) { "fatal: cannot apply associative mapping to table if it wasn't completed".red() }

            table.dependencyTables.add(this.associationTable)

            return null
        }

        @Suppress("UNCHECKED_CAST")
        override fun joinWith(join: ColumnSet): Join =
            join.leftJoin(associationTable)

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
