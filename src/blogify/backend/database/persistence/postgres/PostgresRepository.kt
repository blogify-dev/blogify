package blogify.backend.database.persistence.postgres

import blogify.backend.database.handling.query
import blogify.backend.database.models.QueryContext
import blogify.backend.database.models.ResourceTable
import blogify.backend.database.models.repository
import blogify.backend.database.persistence.models.Repository
import blogify.backend.resources.reflect.update
import blogify.backend.util.*
import blogify.reflect.MappedData
import blogify.reflect.entity.Entity

import org.jetbrains.exposed.sql.*

import java.util.*

open class PostgresRepository<R : Entity>(val table: ResourceTable<R>) : Repository<R> {

    override suspend fun getAll(request: QueryContext, limit: Int): SrList<R>
            = this.table.obtainAll(request, limit)

    override suspend fun queryListing (
        request: QueryContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder
    ): Sr<Pair<List<R>, Boolean>>
            = this.table.obtainListing(request, selectCondition, quantity, page, orderBy, sortOrder)

    override suspend fun get(queryContext: QueryContext, id: UUID): Sr<R>
            = queryContext.entityCache.findOrAsync(id) { table.obtain(queryContext, id).get() }

    override suspend fun getOneMatching (
        queryContext: QueryContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>
    ): Sr<R> = query {
        this.table.select(selectCondition).limit(1).single()
            .let { this.table.convert(queryContext, it).get() }
    }

    override suspend fun add(res: R): Sr<R> = this.table.insert(res)

    override suspend fun update(request: QueryContext, res: R, rawData: MappedData): Sr<R> {
        val new = res.update (
            rawData,
            fetcher = { type, uuid -> request.repository(type).get(request, uuid) }
        ).get()

        this.table.update(new)

        return Wrap { new }
    }

    override suspend fun delete(res: R): Sr<UUID>
            = this.table.delete(res).let { Wrap { res.uuid } }

}
