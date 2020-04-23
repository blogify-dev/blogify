package blogify.backend.database.models

import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.UserCreatedResource
import blogify.backend.util.Sr
import blogify.backend.util.SrList
import blogify.backend.util.Wrap

import org.jetbrains.exposed.sql.*

import java.util.*

abstract class ResourceTable<TResource : Resource> : Table() {

    abstract class UserCreated<TResource : UserCreatedResource> : ResourceTable<TResource>() {
        abstract val authorColumn: Column<UUID>
    }

    suspend fun obtainAll(requestContext: RequestContext, limit: Int): SrList<TResource> = Wrap {
        query { this.selectAll().limit(limit).toSet() }.get()
            .map { this.convert(requestContext, it).get() }
    }

    suspend fun obtainListing (
        requestContext: RequestContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TResource>, Boolean>> = Wrap {

        query {
            this.select(selectCondition)
                .orderBy(orderBy, sortOrder)
                //               v-- We add one to check if we reached the end
                .limit(quantity + 1, (page * quantity).toLong())
                .toList()
        }.get().let { results ->
            results.take(quantity).map { this.convert(requestContext, it).get() } to (results.size - 1 == quantity)
        }

    }

    suspend fun obtain(requestContext: RequestContext, id: UUID): Sr<TResource> = Wrap {
        query { this.select { uuid eq id }.single() }.get()
            .let { this.convert(requestContext, it).get() }
    }

    abstract suspend fun convert(requestContext: RequestContext, source: ResultRow): Sr<TResource>

    abstract suspend fun insert(resource: TResource): Sr<TResource>

    abstract suspend fun update(resource: TResource): Boolean

    open suspend fun delete(resource: TResource): Sr<Boolean> = Wrap {
        query {
            this.deleteWhere { uuid eq resource.uuid }
        }

        true
    }

    val uuid = uuid("uuid")

    override val primaryKey = PrimaryKey(uuid)

}
