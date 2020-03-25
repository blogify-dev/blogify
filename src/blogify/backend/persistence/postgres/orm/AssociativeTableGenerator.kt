package blogify.backend.persistence.postgres.orm

import blogify.backend.persistence.postgres.orm.models.Cardinality
import blogify.backend.persistence.postgres.orm.models.SimpleOrmTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.uuidHandle

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.UUIDColumnType

import java.util.UUID

import kotlin.reflect.KClass

import com.andreapivetta.kolor.red

object AssociativeTableGenerator {

    fun <Trl : Resource> makeAssociativeTable (
        leftHandle:  PropMap.PropertyHandle.Ok<Trl>,
        leftTable:   Table,
        rightTable:  Table,
        cardinality: Cardinality
    ): Table {
        val leftHandleCollectionType = leftHandle.property.returnType.arguments.firstOrNull()?.type
            ?: error("fatal: left handle type (property '${leftHandle.name}') type should have a type parameter".red())

        @Suppress("UNCHECKED_CAST")
        return when(cardinality) {
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ONE_OR_NONE,
            Cardinality.MANY_TO_ONE ->
                error("fatal: makeAssociativeTable should not be called on X_TO_ONE cardinalities".red())
            Cardinality.ONE_TO_MANY ->
                doCreateAssociativeTable (
                    left            = leftHandle.klass.uuidHandle,
                    right           = (leftHandleCollectionType.classifier as KClass<Resource>).uuidHandle,
                    leftMainColumn  = leftTable.columns.first { it.name == "uuid" } as Column<UUID>,
                    rightMainColumn = rightTable.columns.first { it.name == "uuid" } as Column<UUID>,
                    leftUnique      = true
                )
            Cardinality.MANY_TO_MANY ->
                doCreateAssociativeTable (
                    left            = leftHandle.klass.uuidHandle,
                    right           = (leftHandleCollectionType.classifier as KClass<Resource>).uuidHandle,
                    leftMainColumn  = leftTable.columns.first { it.name == "uuid" } as Column<UUID>,
                    rightMainColumn = rightTable.columns.first { it.name == "uuid" } as Column<UUID>,
                    leftUnique      = false
                )
        }
    }

    private fun doCreateAssociativeTable (
        left:            PropMap.PropertyHandle.Ok<*>,
        right:           PropMap.PropertyHandle.Ok<*>,
        leftMainColumn:  Column<UUID>,
        rightMainColumn: Column<UUID>,
        leftUnique:      Boolean
    ): Table {
        val createdTable = SimpleOrmTable(name = "${left.klass.simpleName}_${left.name}_to_${right.klass.simpleName}_${right.name}")

        val leftColumn = createdTable.registerColumn<UUID>(left.klass.simpleName!!, UUIDColumnType())
        leftColumn.foreignKey = ForeignKeyConstraint (
            target = leftMainColumn, from = leftColumn,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.RESTRICT,
            name = "fk_0"
        )

        val rightColumn = createdTable.registerColumn<UUID>(right.klass.simpleName!!, UUIDColumnType())
        rightColumn.foreignKey = ForeignKeyConstraint (
            target = rightMainColumn, from = rightColumn,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.RESTRICT,
            name = "fk_1"
        )

        createdTable.primaryKey = createdTable.PrimaryKey(leftColumn, rightColumn)
        if (leftUnique) createdTable.uniqueIndex(leftColumn)

        return createdTable
    }

}
