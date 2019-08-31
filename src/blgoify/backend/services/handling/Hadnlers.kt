package blgoify.backend.services.handling

import blgoify.backend.database.ResourceTable
import blgoify.backend.resources.models.Resource
import blgoify.backend.services.models.ResourceResult
import blgoify.backend.services.models.ResourceResultSet
import blgoify.backend.services.models.Service
import blgoify.backend.util.query

import com.github.kittinunf.result.coroutines.map
import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.util.UUID

/**
 * Retrieves all the [resources][Resource] of a certain type from the DB.
 *
 * @param table the [ResourceTable] to query
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 */
suspend fun <R : Resource> handleResourceDBFetchAll(table: ResourceTable<R>): ResourceResultSet<R> {
    return query {
        table.selectAll().toSet() // First, query the DB
    }
        .map { rows ->                                   // Map the set of ResultRow to converted resources
            rows.map { table.convert(it).get() }.toSet() //     Mote : get() is fine, since any error thrown
        }                                                //            by it is automatically wrapped into a failure result.

        .mapError { e -> Service.Exception.Fetching(e) } // Wrap an eventual DBEx inside a Service exception
}

/**
 * Retrieves a [resource][Resource] of a certain type from the DB.
 *
 * @param table      the [ResourceTable] to query
 * @param uuidColumn the column of [table] containing the [resources][Resource] [UUID]
 * @param id         the [UUID] of the resource to fetch.
 *
 * @return a [ResourceResultSet] that represents the success of the query, with a Database.Exception wrapped in if necessary.
 */
suspend fun <R : Resource> handleResourceDBFetch(table: ResourceTable<R>, uuidColumn: Column<UUID>, id: UUID): ResourceResult<R> {
    return query {
        table.select { uuidColumn eq id }.single() // First, query the DB
    }
        .map      { r -> table.convert(r).get() }        // Map the ResultRow to a converted resource. See note above.
        .mapError { e -> Service.Exception.Fetching(e) } // Wrap an eventual DBEx inside a Service exception
}