package blogify.backend.persistence.postgres

import blogify.backend.database.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.update
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.SrList
import blogify.backend.util.getOrPipelineError

import io.ktor.http.HttpStatusCode

import com.github.kittinunf.result.coroutines.map

import kotlinx.coroutines.runBlocking

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import java.util.*

open class PostgresRepository<R : Resource>(val table: ResourceTable<R>) : Repository<R> {

    override suspend fun getAll(request: RequestContext, limit: Int): SrList<R>
            = this.table.obtainAll(request, limit)

    override suspend fun queryListing(request: RequestContext, selectCondition: SqlExpressionBuilder.() -> Op<Boolean>, quantity: Int, page: Int): Sr<Pair<List<R>, Boolean>>
            = this.table.obtainListing(request, selectCondition, quantity, page, this.table.uuid)

    override suspend fun get(request: RequestContext, id: UUID): Sr<R>
            = request.cache.findOrAsync(id) { table.obtain(request, id).get() }

    override suspend fun getMatching(request: RequestContext, predicate: SqlExpressionBuilder.() -> Op<Boolean>): SrList<R> {
        return Wrap {
            transaction {
                val query = table.select(predicate).toSet()
                runBlocking { query.map { table.convert(request, it).get() }.toList() }
            }
        }
    }

    override suspend fun add(res: R): Sr<R> = this.table.insert(res)

    override suspend fun update(request: RequestContext, res: R, rawData: Map<PropMap.PropertyHandle.Ok, Any?>): Sr<R> {
        val new = update(res, request, rawData)
            .getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't update resource")

        this.table.update(new)

        return Wrap { new }
    }

    override suspend fun delete(res: R): Sr<UUID>
            = this.table.delete(res).map { res.uuid }

}
