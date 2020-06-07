package blogify.backend.database.models

import blogify.backend.annotations.table
import blogify.backend.persistence.models.Repository
import blogify.backend.persistence.postgres.PostgresRepository
import blogify.backend.resources.models.Resource
import blogify.backend.util.MapCache
import blogify.backend.util.Sr
import blogify.backend.util.SrList
import blogify.backend.util.assertGet

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import java.util.*

import kotlin.reflect.KClass

interface QueryContext {

    val repositoryCache: MapCache<KClass<out Resource>, Repository<out Resource>>

    val entityCache: MapCache<UUID, Resource>

    suspend fun <TResource : Resource> Repository<TResource>.obtainAll(limit: Int): SrList<TResource> =
        this.getAll(this@QueryContext, limit)

    suspend fun <TResource : Resource> Repository<TResource>.obtain(id: UUID): Sr<Resource> =
        this@QueryContext.entityCache.findOrAsync(id) {
            this.get(this@QueryContext, id).get()
        }

    suspend fun <TResource : Resource> Repository<TResource>.obtainListing (
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TResource>, Boolean>> =
        this.queryListing(this@QueryContext, selectCondition, quantity, page, orderBy, sortOrder)

}

@Suppress("UNCHECKED_CAST")
inline fun <reified TResource : Resource> QueryContext.repository(): Repository<TResource> =
    this.repositoryCache.findOr(TResource::class) {
        PostgresRepository(TResource::class.table)
    }.assertGet()

@Suppress("UNCHECKED_CAST")
fun <TResource : Resource> QueryContext.repository(klass: KClass<out TResource>): Repository<out TResource> =
    this.repositoryCache.findOr(klass) {
        PostgresRepository(klass.table)
    }.assertGet()
