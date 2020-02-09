package blogify.backend.persistence.sql

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.routing.pipelines.caching.cachedOrElse
import blogify.backend.services.models.Repository
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.SrList
import blogify.backend.util.getOrPipelineError

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode

import com.github.kittinunf.result.coroutines.map

import kotlinx.coroutines.runBlocking

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import org.slf4j.LoggerFactory

import java.util.*

open class SqlRepository<R : Resource>(val table: ResourceTable<R>) : Repository<R> {

    private val logger = LoggerFactory.getLogger("blogify-service-${this::class.simpleName}")

    override suspend fun getAll(callContext: ApplicationCall, limit: Int): SrList<R>
            = this.table.obtainAll(callContext, limit)

    override suspend fun get(callContext: ApplicationCall, id: UUID): Sr<R>
            = callContext.cachedOrElse(id) { table.obtain(callContext, id) }

    override suspend fun getMatching(callContext: ApplicationCall, predicate: SqlExpressionBuilder.() -> Op<Boolean>): SrList<R> {
        return Wrap {
            transaction {
                val query = table.select(predicate).toSet()
                runBlocking { query.map { table.convert(callContext, it).get() }.toList() }
            }
        }
    }

    override suspend fun add(res: R): Sr<R> = this.table.insert(res)

    override suspend fun update(res: R, rawData: Map<PropMap.PropertyHandle.Ok, Any?>): Sr<R> {
        val new = blogify.backend.resources.reflect.update(res, rawData)
            .getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't update resource")

        this.table.update(new)

        return Wrap { new }
    }

    override suspend fun delete(res: R): Sr<UUID>
            = this.table.delete(res).map { res.uuid }

}
