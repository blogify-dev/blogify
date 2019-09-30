package blogify.backend.services.handling

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.map
import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.util.UUID

/**
 * Retrieves all the [resources][Resource] from a certain table of the DB.
 *
 * @param table the [ResourceTable] to query
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 */
suspend fun <R : Resource> fetchAllFromTable(table: ResourceTable<R>): ResourceResultSet<R> {
    return query {
        table.selectAll().toSet() // First, query the DB
    }
        .map { rows ->                                   // Map the set of ResultRow to converted resources
            rows.map { table.convert(it).get() }.toSet() //     Mote : get() is fine, since any error thrown
        }                                                //            by it is automatically wrapped into a failure result.

        .mapError { e -> Service.Exception.Fetching(e) } // Wrap a possible DBEx inside a Service exception
}

/**
 * Retrieves a number of [resources][Resource] from a certain table of the DB.
 *
 * @param table the [ResourceTable] to query
 * @param limit the number of [resources][Resource] to fetch
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 */
suspend fun <R : Resource> fetchNumberFromTable(table: ResourceTable<R>, limit: Int): ResourceResultSet<R> {
    return query {
        table.selectAll().take(limit).toSet()
    }
        .map { rows ->                                   // Map the set of ResultRow to converted resources
            rows.map { table.convert(it).get() }.toSet() //     Mote : get() is fine, since any error thrown
        }                                                //            by it is automatically wrapped into a failure result.

        .mapError { e -> Service.Exception.Fetching(e) } // Wrap a possible DBEx inside a Service exception
}

/**
 * Retrieves a [resource][Resource] of a certain type from the DB.
 *
 * @param table      the [ResourceTable] to query
 * @param uuidColumn the column of [table] containing the [resources][Resource] [UUID]
 * @param id         the [UUID] of the resource to fetch
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 */
suspend fun <R : Resource> fetchWithIdFromTable(table: ResourceTable<R>, uuidColumn: Column<UUID>, id: UUID): ResourceResult<R> {
    return query {
        table.select { uuidColumn eq id }.single() // First, query the DB
    }
        .map      { r -> table.convert(r).get() }        // Map the ResultRow to a converted resource. See note above.
        .mapError { e -> Service.Exception.Fetching(e) } // Wrap a possible DBEx inside a Service exception
}

/**
 * Retrieves a certain [resource][Resource] from the DB.
 *
 * @param table      the [ResourceTable] to act on
 * @param uuidColumn the column of [table] containing the [resources][Resource] [UUID]
 * @param id         the [UUID] of the resource to delete
 *
 * @return a [ResourceResult] that represents the success of the deletion, with a Database.Exception wrapped in if necessary.
 */
suspend fun <R : Resource> deleteWithIdInTable(table: ResourceTable<R>, uuidColumn: Column<UUID>, id: UUID): ResourceResult<UUID> {
    return query {
        table.deleteWhere { uuidColumn eq id } // First, instruct the DB to delete the corresponding row
        return@query id
    }
        .mapError { e -> Service.Exception.Deleting(e) } // Wrap a possible DBEx inside a Service exception
}
