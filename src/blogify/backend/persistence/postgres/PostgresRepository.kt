package blogify.backend.persistence.postgres

import blogify.backend.database.handling.query
import blogify.backend.database.models.ResourceTable
import blogify.backend.resources.models.Resource
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.update
import blogify.backend.util.*
import blogify.reflect.MappedData

import org.jetbrains.exposed.sql.*

import io.ktor.http.HttpStatusCode

import java.util.*

open class PostgresRepository<R : Resource>(val table: ResourceTable<R>) : Repository<R> {

    override suspend fun getAll(request: RequestContext, limit: Int): SrList<R>
            = this.table.obtainAll(request, limit)

    override suspend fun queryListing (
        request: RequestContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder
    ): Sr<Pair<List<R>, Boolean>>
            = this.table.obtainListing(request, selectCondition, quantity, page, orderBy, sortOrder)

    override suspend fun get(request: RequestContext, id: UUID): Sr<R>
            = request.cache.findOrAsync(id) { table.obtain(request, id).get() }

    override suspend fun getOneMatching (
        request: RequestContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>
    ): Sr<R> = query {
        this.table.select(selectCondition).limit(1).single()
            .let { this.table.convert(request, it).get() }
    }

    override suspend fun add(res: R): Sr<R> = this.table.insert(res)

    override suspend fun update(request: RequestContext, res: R, rawData: MappedData): Sr<R> {
        val new = res.update (
            rawData,
            fetcher = { type, uuid -> request.repository(type).get(request, uuid) }
        ).getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't update resource")

        this.table.update(new)

        return Wrap { new }
    }

    override suspend fun delete(res: R): Sr<UUID>
            = this.table.delete(res).let { Wrap { res.uuid } }

}
