package blogify.backend.services.models

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.Resource.ObjectResolver.FakeApplicationCall
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.services.caching.cachedOrElse
import blogify.backend.util.BException
import blogify.backend.util.Sr
import blogify.backend.util.SrList
import blogify.backend.util.getOrPipelineError

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.map
import com.github.kittinunf.result.coroutines.mapError

import kotlinx.coroutines.runBlocking

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
open class Service<R : Resource>(val table: ResourceTable<R>) {

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
    suspend fun getAll(callContext: ApplicationCall = FakeApplicationCall, limit: Int = 256): SrList<R>
            = this.table.obtainAll(callContext, limit)

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
    suspend fun get(callContext: ApplicationCall = FakeApplicationCall, id: UUID): Sr<R>
            = callContext.cachedOrElse(id) { table.obtain(callContext, id) }

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

    suspend fun add(res: R): Sr<R> = this.table.insert(res)

    suspend fun update(res: R, rawData: Map<PropMap.PropertyHandle.Ok, Any?>): SuspendableResult<R, Exception> {
        val new = blogify.backend.resources.reflect.update(res, rawData)
            .getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't update resource")

        this.table.update(new)

        return Sr.of { new }
    }

    /**
     * Deletes an instance of [R] from the database
     *
     * @param id the [UUID] of the resource to fetch
     *
     * @return a [ResourceResultSet] of the [UUID] of the deleted item
     *
     * @author Benjozork, hamza1311
     */
    suspend fun delete(res: R): Sr<UUID>
            = this.table.delete(res).map { res.uuid }

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
