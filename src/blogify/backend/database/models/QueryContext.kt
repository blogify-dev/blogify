package blogify.backend.database.models

import blogify.backend.persistence.models.Repository
import blogify.backend.resources.models.Resource
import blogify.backend.util.MapCache
import blogify.backend.util.Sr
import blogify.backend.util.SrList

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import java.util.*

import kotlin.reflect.KClass

interface QueryContext {

    val repositoryCache: MapCache<KClass<out Resource>, Repository<Resource>>

    val entityCache: MapCache<UUID, Resource>

    suspend fun <TResource : Resource> ResourceTable<TResource>.obtainAll(limit: Int): SrList<TResource> =
        this.obtainAll(this@QueryContext, limit)

    suspend fun <TResource : Resource> ResourceTable<TResource>.obtain(id: UUID): Sr<Resource> =
        this@QueryContext.entityCache.findOrAsync(id) {
            this.obtain(this@QueryContext, id).get()
        }

    suspend fun <TResource : Resource> ResourceTable<TResource>.obtainListing (
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TResource>, Boolean>> =
        this.obtainListing(this@QueryContext, selectCondition, quantity, page, orderBy, sortOrder)

}

@Suppress("UNCHECKED_CAST")
inline fun <reified TResource : Resource> QueryContext.repository(): Repository<TResource> =
    this.repositoryCache[TResource::class] as Repository<TResource>

@Suppress("UNCHECKED_CAST")
inline fun <reified TResource : Resource> QueryContext.repository(klass: KClass<out TResource>): Repository<TResource> =
    this.repositoryCache[TResource::class] as Repository<TResource>

