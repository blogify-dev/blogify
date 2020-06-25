package blogify.database.persistence.postgres

import blogify.common.util.Sr
import blogify.common.util.SrList
import blogify.common.util.Wrap
import reflectr.util.MappedData
import reflectr.entity.Entity
import blogify.database.handling.query
import blogify.database.QueryContext
import blogify.database.EntityTable
import blogify.database.persistence.models.Repository

import org.jetbrains.exposed.sql.*

import java.util.*

@ExperimentalStdlibApi
open class PostgresRepository<R : Entity>(val table: EntityTable<R>) :
    Repository<R> {

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

    override suspend fun update(request: QueryContext, res: R, rawData: MappedData): Sr<R> =
        Wrap {
            val new = with(request) {
                res.update(rawData)
            }.get()

            this.table.update(new).get()
        }

    override suspend fun delete(entity: R): Sr<UUID>
            = this.table.delete(entity).let { Wrap { entity.uuid } }

}
