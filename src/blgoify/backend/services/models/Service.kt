package blgoify.backend.services.models

import blgoify.backend.database.ResourceTable
import blgoify.backend.resources.models.Resource
import blgoify.backend.util.BException
import blgoify.backend.util.query

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select

import java.util.*

typealias ResourceResult<T> = SuspendableResult<T, Service.Exception>

typealias ResourceResultSet<T> = ResourceResult<Set<T>>

interface Service<R : Resource> {

    suspend fun getAll(): ResourceResultSet<R>

    suspend fun get(id: UUID): ResourceResult<R>

    suspend fun getMatching(table: ResourceTable<R>, predicate: SqlExpressionBuilder.() -> Op<Boolean>): ResourceResultSet<R> {
        return query {
            table.select(predicate).toSet().map { table.convert(it).get() }.toSet()
        }.mapError { Exception.Fetching(it) }
    }

    suspend fun add(res: R): ResourceResult<R>

    suspend fun remove(id: UUID): ResourceResult<UUID>

    suspend fun update(res: R): ResourceResult<R>

    // Service exceptions

    open class Exception(causedBy: BException) : BException(causedBy) {

        open class Fetching(causedBy: BException) : Exception(causedBy) {

            class NotFound(causedBy: BException) : Fetching(causedBy)

        }

        open class Creating(causedBy: BException) : Exception(causedBy)

    }

}