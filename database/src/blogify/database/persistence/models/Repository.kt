package blogify.database.persistence.models

import blogify.common.util.Sr
import blogify.common.util.SrList
import reflectr.util.MappedData
import reflectr.models.PropMap
import reflectr.extensions.okHandle
import blogify.database.QueryContext
import reflectr.entity.Entity

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import java.util.*

import kotlin.reflect.KProperty1

/**
 * Service interface for fetching, creating, updating and deleting [entities][Entity].
 */
interface Repository<R : Entity> {

    /**
     * Obtains all instances of [R] in the database
     *
     * @param request the [QueryContext] to use for this operation, for caching and querying purposes
     * @param limit   the max number of items to fetch. Defaults to 256.
     *
     * @author Benjozork, hamza1311
     */
    suspend fun getAll(request: QueryContext, limit: Int = 256): SrList<R>

    /**
     * Obtains an instance of [R] with a specific [id][UUID] ]in the database
     *
     * @param request         the [QueryContext] to use for this operation, for caching and querying purposes
     * @param selectCondition the SQL predicate to match rows with
     * @param quantity        the quantity of items to fetch on one page
     * @param page            the page number to fetch (group of [quantity] items)
     * @param orderBy         the SQL column to order the entities by
     * @param sortOrder       the sort order to sort the entities in
     *
     * @author Benjozork, hamza1311
     */
    suspend fun queryListing(request: QueryContext,
                             selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
                             quantity: Int,
                             page: Int,
                             orderBy: Column<*>,
                             sortOrder: SortOrder = SortOrder.ASC): Sr<Pair<List<R>, Boolean>>

    /**
     * Obtains an instance of [R] with a specific [id][UUID] ]in the database
     *
     * @param queryContext the [QueryContext] to use for this operation, for caching and querying purposes
     * @param id           the [UUID] of the entity to fetch
     *
     * @author Benjozork, hamza1311
     */
    suspend fun get(queryContext: QueryContext, id: UUID): Sr<R>

    /**
     * Obtains an instance of [R] matching a specific SQL predicate
     *
     * @param queryContext the [QueryContext] to use for this operation, for caching and querying purposes
     * @param selectCondition the SQL predicate to match rows with
     *
     * @author Benjozork, hamza1311
     */
    suspend fun getOneMatching(queryContext: QueryContext, selectCondition: SqlExpressionBuilder.() -> Op<Boolean>): Sr<R>

    /**
     * Adds an instance of [R] to the database
     *
     * @param res the entity to add
     *
     * @author Benjozork, hamza1311
     */
    suspend fun add(res: R): Sr<R>

    /**
     * Updates an instance of [R] in the database
     *
     * @param request the [QueryContext] to use for this operation, for caching and querying purposes
     * @param res     the entity to update
     * @param rawData a map of [property handles][PropMap.PropertyHandle.Ok] to replacement values. Can omit values to not update them.
     *
     * @author Benjozork, hamza1311
     */
    suspend fun update(request: QueryContext, res: R, rawData: MappedData): Sr<R>

    /**
     * Updates an instance of [R] in the database
     *
     * @param request  the [QueryContext] to use for this operation, for caching and querying purposes
     * @param entity   the entity to update
     * @param data     a map of [properties][KProperty1] to replacement values. Properties must resolve
     *                 to [Ok][PropMap.PropertyHandle.Ok] handles. Can omit values to not update them.
     *
     * @author Benjozork, hamza1311
     */
    suspend fun updateWithProperties(request: QueryContext, entity: R, data: Map<out KProperty1<R, Any>, Any>): Sr<R>
            = update(request, entity, data.mapKeys { it.key.okHandle ?: error("update with a <KProperty1, Any> map cannot resolve to non-ok handles") })

    /**
     * Deletes an instance of [R] from the database
     *
     * @param entity the entity to delete
     *
     * @author Benjozork, hamza1311
     */
    suspend fun delete(entity: R): Sr<UUID>

}
