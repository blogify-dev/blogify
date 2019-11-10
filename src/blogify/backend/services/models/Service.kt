package blogify.backend.services.models

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.Resource.ObjectResolver.FakeApplicationCall
import blogify.backend.services.caching.cachedOrElse
import blogify.backend.services.handling.countReferringInTable
import blogify.backend.services.handling.deleteWithIdInTable
import blogify.backend.services.handling.fetchNumberFromTable
import blogify.backend.services.handling.fetchWithIdFromTable
import blogify.backend.util.BException

import io.ktor.application.ApplicationCall

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.mapError

import kotlinx.coroutines.runBlocking

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import org.slf4j.LoggerFactory

import java.util.*

typealias ResourceResult<T> = SuspendableResult<T, Service.Exception>

typealias ResourceResultSet<T> = ResourceResult<Set<T>>

/**
 * Service interface for fetching, creating, updating and deleting [resources][Resource].
 */
abstract class Service<R : Resource>(val table: ResourceTable<R>) {

    private val logger = LoggerFactory.getLogger("blogify-service-${this::class.simpleName}")

    /**
     * Obtains all instances of [R] in the database
     *
     * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
     *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
     *
     * @param limit the max number of items to fetch. Defaults to 256.
     *
     * @return a [ResourceResultSet] of [R] items
     *
     * @author Benjozork, hamza1311
     */
    suspend fun getAll(callContext: ApplicationCall = FakeApplicationCall, limit: Int = 256): ResourceResultSet<R>
            = fetchNumberFromTable(callContext, table, limit)

    /**
     * Obtains an instance of [R] with a specific [id][UUID] ]in the database
     *
     * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
     *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
     *
     * @param id the [UUID] of the resource to fetch
     *
     * @return a [ResourceResult] of an [R] item with the provided [id]
     *
     * @author Benjozork, hamza1311
     */
    suspend fun get(callContext: ApplicationCall = FakeApplicationCall, id: UUID): ResourceResult<R>
            = callContext.cachedOrElse(id) { fetchWithIdFromTable(callContext, table, id) }

    /**
     * Obtains a set of instances of [R] matching a given [predicate]
     *
     * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
     *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
     *
     * @param predicate an Exposed predicate that is used to return the needed items
     *
     * @return a [ResourceResultSet] of [R] items matching [predicate]
     *
     * @author hamza1311
     */
    suspend fun getMatching(callContext: ApplicationCall = FakeApplicationCall, predicate: SqlExpressionBuilder.() -> Op<Boolean>): ResourceResultSet<R> {
        return SuspendableResult.of<Set<R>, Exception> {
            transaction {
                val query = table.select(predicate).toSet()
                runBlocking { query.map { table.convert(callContext, it).get() }.toSet() }
            }
        }.mapError { Exception.Fetching(it) }
    }

    /**
     * Returns the number of [R] that refer to [withValue] in [table].
     *
     * @param inField   the column of [table] in which the reference is stored
     * @param withValue the [Resource] to count occurrences of
     *
     * @return the number of instances of [withValue] in [table]
     *
     * @author Benjozork
     */
    suspend fun <T : Resource> getReferring(inField: Column<UUID>, withValue: T)
            = countReferringInTable(table, inField, withValue.uuid)

    abstract suspend fun add(res: R): ResourceResult<R>

    abstract suspend fun update(res: R): SuspendableResult<*, Exception>

    /**
     * Deletes an instance of [R] from the database
     *
     * @param id the [UUID] of the resource to fetch
     *
     * @return a [ResourceResultSet] of the [UUID] of the deleted item
     *
     * @author Benjozork, hamza1311
     */
    suspend fun delete(id: UUID): ResourceResult<UUID>
            = deleteWithIdInTable(table, id)

    // Service exceptions

    open class Exception(causedBy: BException) : BException(causedBy) {

        open class Fetching(causedBy: BException) : Exception(causedBy) {

            class NotFound(causedBy: BException) : Fetching(causedBy)

        }

        open class Creating(causedBy: BException) : Exception(causedBy)

        open class Deleting(causedBy: BException) : Exception(causedBy)
        open class Updating(causedBy: BException) : Exception(causedBy)

    }

}
