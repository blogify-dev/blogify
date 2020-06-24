package blogify.reflect.entity.database

import blogify.common.util.Sr
import blogify.common.util.SrList
import blogify.common.util.MapCache
import blogify.reflect.MappedData
import blogify.reflect.entity.Entity
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.entity.database.persistence.models.Repository
import blogify.reflect.entity.update
import blogify.reflect.entity.instantiation.construct
import blogify.reflect.entity.database.extensions.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import com.fasterxml.jackson.databind.ObjectMapper

import java.util.*

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Interface that allows creating a context in which entities are queried, created or modified.
 *
 * Allows for in-request caching of entities by their IDs. It also brings in scope extension functions to [EntityTable]
 * as well as overloads for various reflection functions like [construct][blogify.reflect.entity.instantiation.construct] and
 * [update][blogify.reflect.entity.update] that allow a consumer with an implementation of this as receiver to call
 * those functions without passing in lookup functions or [QueryContext] instances.
 *
 * @author Benjozork
 */
interface QueryContext {

    val databaseContext: DatabaseContext

    val entityCache: MapCache<UUID, Entity>

    val objectMapper: ObjectMapper

    /** See [blogify.reflect.entity.database.persistence.models.Repository.getAll] */
    suspend fun <TEntity : Entity> Repository<TEntity>.obtainAll(limit: Int): SrList<TEntity> =
        this.getAll(this@QueryContext, limit)

    /** See [blogify.reflect.entity.database.persistence.models.Repository.get] */
    suspend fun <TEntity : Entity> Repository<TEntity>.obtain(id: UUID): Sr<TEntity> =
        this@QueryContext.entityCache.findOrAsync(id) {
            this.get(this@QueryContext, id).get()
        }

    /** See [blogify.reflect.entity.database.persistence.models.Repository.get] */
    suspend fun <TEntity : Entity> Repository<TEntity>.obtainListing (
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TEntity>, Boolean>> =
        this.queryListing(this@QueryContext, selectCondition, quantity, page, orderBy, sortOrder)

    /** See [blogify.reflect.entity.database.persistence.models.Repository.update] */
    suspend fun <TEntity : Entity> Repository<TEntity>.update(res: TEntity, rawData: MappedData): Sr<TEntity> =
        this.update(this@QueryContext, res, rawData)

    /** See [blogify.reflect.entity.database.persistence.models.Repository.updateWithProperties] */
    suspend fun <TEntity : Entity> Repository<TEntity>.updateWithProperties (
        entity: TEntity,
        data: Map<out KProperty1<TEntity, Any>, Any>
    ): Sr<TEntity> =
        this.updateWithProperties(this@QueryContext, entity, data)

    /** See [blogify.reflect.entity.instantiation.construct] */
    suspend fun <TMapped : Mapped> KClass<out TMapped>.construct (
        data:               MappedData,
        externallyProvided: Set<PropMap.PropertyHandle.Ok> = setOf()
    ): Sr<TMapped> =
        this.construct(data, objectMapper, { klass, uuid -> this@QueryContext.repository(klass).get(queryContext = this@QueryContext, id = uuid) }, externallyProvided)

    /** See [blogify.reflect.entity.update] */
    suspend fun <R : Mapped> R.update(rawData: MappedData): Sr<R> =
        this.update(rawData, this@QueryContext.objectMapper, fetcher = { klass, id -> this@QueryContext.repository(klass).get(id = id, queryContext = this@QueryContext) })

}

