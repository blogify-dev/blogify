package blogify.backend.services.models

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.util.BException
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.mapError
import kotlinx.coroutines.runBlocking

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import java.util.*

typealias ResourceResult<T> = SuspendableResult<T, Service.Exception>

typealias ResourceResultSet<T> = ResourceResult<Set<T>>

interface Service<R : Resource> {

    suspend fun getAll(): ResourceResultSet<R>

    suspend fun get(id: UUID): ResourceResult<R>

    suspend fun getMatching(table: ResourceTable<R>, predicate: SqlExpressionBuilder.() -> Op<Boolean>): ResourceResultSet<R> {
        return SuspendableResult.of<Set<R>, Exception> {
            transaction {
                val query = table.select(predicate).toSet()
                runBlocking { query.map { table.convert(it).get() }.toSet() }
            }
        }.mapError { Exception.Fetching(it) }
    }

    suspend fun add(res: R): ResourceResult<R>

    suspend fun delete(id: UUID): ResourceResult<UUID>

    suspend fun update(res: R): ResourceResult<R>

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