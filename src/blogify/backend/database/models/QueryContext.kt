package blogify.backend.database.models

import blogify.backend.annotations.table
import blogify.backend.database.persistence.models.Repository
import blogify.backend.database.persistence.postgres.PostgresRepository
import blogify.backend.resources.reflect.construct
import blogify.backend.resources.reflect.update
import blogify.backend.util.MapCache
import blogify.backend.util.Sr
import blogify.backend.util.SrList
import blogify.backend.util.assertGet
import blogify.reflect.MappedData
import blogify.reflect.entity.Entity
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import java.util.*

import kotlin.reflect.KClass

/**
 * Interface that allows creating a context in which entities are queried, created or modified.
 *
 * Allows for in-request caching of entities by their IDs. It also brings in scope extension functions to [ResourceTable]
 * as well as overloads for various reflection functions like [construct][blogify.backend.resources.reflect.construct] and
 * [update][blogify.backend.resources.reflect.update] that allow a consumer with an implementation of this as receiver to call
 * those functions without passing in lookup functions or [QueryContext] instances.
 *
 * @author Benjozork
 */
interface QueryContext {

    val repositoryCache: MapCache<KClass<out Entity>, Repository<out Entity>>

    val entityCache: MapCache<UUID, Entity>

    suspend fun <TResource : Entity> Repository<TResource>.obtainAll(limit: Int): SrList<TResource> =
        this.getAll(this@QueryContext, limit)

    suspend fun <TResource : Entity> Repository<TResource>.obtain(id: UUID): Sr<Entity> =
        this@QueryContext.entityCache.findOrAsync(id) {
            this.get(this@QueryContext, id).get()
        }

    suspend fun <TResource : Entity> Repository<TResource>.obtainListing (
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TResource>, Boolean>> =
        this.queryListing(this@QueryContext, selectCondition, quantity, page, orderBy, sortOrder)


    suspend fun <TMapped : Mapped> KClass<out TMapped>.construct (
        data:               MappedData,
        externallyProvided: Set<PropMap.PropertyHandle.Ok> = setOf()
    ): Sr<TMapped> = this.construct(data, { klass, uuid -> this@QueryContext.repository(klass).get(queryContext = this@QueryContext, id = uuid) }, externallyProvided)

    suspend fun <R : Mapped> R.update(rawData: MappedData): Sr<R> =
        this.update(rawData, fetcher = { klass, id -> this@QueryContext.repository(klass).get(id = id, queryContext = this@QueryContext) })

}

@Suppress("UNCHECKED_CAST")
inline fun <reified TResource : Entity> QueryContext.repository(): Repository<TResource> =
    this.repositoryCache.findOr(TResource::class) {
        PostgresRepository(TResource::class.table)
    }.assertGet()

@Suppress("UNCHECKED_CAST")
fun <TResource : Entity> QueryContext.repository(klass: KClass<out TResource>): Repository<out TResource> =
    this.repositoryCache.findOr(klass) {
        PostgresRepository(klass.table)
    }.assertGet()
