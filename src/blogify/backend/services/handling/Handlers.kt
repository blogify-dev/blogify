package blogify.backend.services.handling

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query
import blogify.backend.util.Sr

import io.ktor.application.ApplicationCall

import com.github.kittinunf.result.coroutines.map
import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.lang.Exception
import java.util.UUID

/**
 * Retrieves all the [resources][Resource] from a certain table of the DB.
 *
 * @param table the [ResourceTable] to query
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 *
 * @author Benjozork
 */
suspend fun <R : Resource> fetchAllFromTable(callContext: ApplicationCall, table: ResourceTable<R>): ResourceResultSet<R> {
    return query {
        table.selectAll().toSet() // First, query the DB
    }
        .mapError { e -> Service.Exception.Fetching(e) } // Wrap a possible DBEx inside a Service exception
        .map { rows ->                                   // Map the set of ResultRow to converted resources
            rows.map { table.convert(callContext, it).get() }.toSet() //     Mote : get() is fine, since any error thrown
        }                                                //            by it is automatically wrapped into a failure result.
}

/**
 * Retrieves a number of [resources][Resource] from a certain table of the DB.
 *
 * @param callContext the [ApplicationCall] in context
 * @param table       the [ResourceTable] to query
 * @param limit       the number of [resources][Resource] to fetch
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 *
 * @author Benjozork
 */
suspend fun <R : Resource> fetchNumberFromTable(callContext: ApplicationCall, table: ResourceTable<R>, limit: Int): ResourceResultSet<R> {
    return query {
        table.selectAll().take(limit).toSet()
    }
        .mapError { e -> Service.Exception.Fetching(e) } // Wrap a possible DBEx inside a Service exception
        .map { rows ->                                   // Map the set of ResultRow to converted resources
            rows.map { table.convert(callContext, it).get() }.toSet() // Mote : get() is fine, since any error thrown
        }                                                             //  by it is automatically wrapped into a failure result.
}

/**
 * Retrieves a [resource][Resource] of a certain type from the DB.
 *
 * @param callContext the [ApplicationCall] in context
 * @param table       the [ResourceTable] to query
 * @param id          the [UUID] of the resource to fetch
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 *
 * @author Benjozork
 */
suspend fun <R : Resource> fetchWithIdFromTable(callContext: ApplicationCall, table: ResourceTable<R>, id: UUID): ResourceResult<R> {
    return query {
        table.select { table.uuid eq id }.single() // First, query the DB
    }
        .mapError { e -> Service.Exception.Fetching(e) } // Wrap a possible DBEx inside a Service exception
        .map      { r -> table.convert(callContext, r).get() }        // Map the ResultRow to a converted resource. See note above.
}

/**
 * Deletes a certain [resource][Resource] from the DB.
 *
 * @param table the [ResourceTable] to act on
 * @param id    the [UUID] of the resource to delete
 *
 * @return a [ResourceResult] that represents the success of the deletion, with a Database.Exception wrapped in if necessary.
 *
 * @author Benjozork
 */
suspend fun <R : Resource> deleteWithIdInTable(table: ResourceTable<R>, id: UUID): ResourceResult<UUID> {
    return query {
        table.deleteWhere { table.uuid eq id } // First, instruct the DB to delete the corresponding row
        return@query id
    }
        .mapError { e -> Service.Exception.Deleting(e) } // Wrap a possible DBEx inside a Service exception
}

/**
 * Counts the number of references of a certain value in a provided column of a table
 *
 * @param referenceField the column in which those references to [referenceValue] are to be counted.
 *
 * @return the number of rows of [referenceField]'s table in which [referenceValue] appears in [referenceField]
 *
 * @author Benjozork
 */
suspend fun <A : Any> countReferences (
    referenceField: Column<A>,
    referenceValue: A,
    where:          SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
): ResourceResult<Int> {
    return query {
        referenceField.table.select { referenceField eq referenceValue and where() }.count()
    }
        .mapError { e -> Service.Exception(e) }
}

/**
 * Counts the number of references for every value of a column in another provided column.
 *
 * @param originField the column in which the values to count references to are stored
 * @param secondField the column in which references to each value of [originField] are stored
 *
 * @return a map of all the values of [originField] to the number of references to that value in [secondField]
 *
 * @author Benjozork
 */
suspend fun <A : Any> countAllReferences (
    originField: Column<A>,
    secondField: Column<A>,
    where:       SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
): Sr<Map<A, Int>, Exception> {
    return query {
        val refCountColumn = secondField.count()
        originField.table
            .leftJoin(secondField.table, { originField }, { secondField })
            .slice(originField, refCountColumn)
            .select(where)
            .groupBy(originField)
            .toSet().map { it[originField] to it[refCountColumn] }.toMap()
    }
}
suspend infix fun <A : Any> Column<A>.referredToBy(other: Column<A>) = countAllReferences(this, other).get()
