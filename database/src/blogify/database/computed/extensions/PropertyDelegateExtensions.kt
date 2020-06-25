package blogify.database.computed.extensions

import reflectify.entity.Entity
import blogify.database.computed.AggregateComputedPropertyContainer

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Table

import java.util.*

/**
 * Creates an [AggregateComputedPropertyContainer] computed property container. Allows for computing the result
 * of an aggregation function for [this] on [another table][rightTable].
 *
 * Will be optimized into listing queries by the [query optimizer][blogify.database.optimizer.QueryOptimizer] in the future.
 *
 * @param aggregateExpr the aggregate [Expression] that will be included after `SELECT` in the query
 * @param rightTable    the table on which the aggregation is performed
 * @param rightColumn   the column of [rightTable] that must match the UUID of [this]
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
fun <TEntity : Entity, TProperty : Any?> TEntity.aggregate (
    aggregateExpr: Expression<TProperty>,
    rightTable: Table,
    rightColumn: Column<UUID>
): AggregateComputedPropertyContainer<TEntity, TProperty> =
    AggregateComputedPropertyContainer(this, aggregateExpr, rightTable, rightColumn)
