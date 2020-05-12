package blogify.backend.persistence.models

import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.Resource.ObjectResolver.FakeRequestContext
import blogify.backend.util.*
import blogify.reflect.models.PropMap
import blogify.reflect.extensions.okHandle

import io.ktor.application.ApplicationCall

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import java.util.*

import kotlin.reflect.KProperty1

/**
 * Service interface for fetching, creating, updating and deleting [resources][Resource].
 */
interface Repository<R : Resource> {

    /**
     * Obtains all instances of [R] in the database
     *
     * @param request the context of the [call][ApplicationCall] resulting in this operation, used for caching purposes
     * @param limit   the max number of items to fetch. Defaults to 256.
     *
     * @author Benjozork, hamza1311
     */
    suspend fun getAll(request: RequestContext = FakeRequestContext, limit: Int = 256): SrList<R>

    /**
     * Obtains an instance of [R] with a specific [id][UUID] ]in the database
     *
     * @param request         the context of the [call][ApplicationCall] resulting in this operation, used for caching purposes
     * @param selectCondition the SQL predicate to match rows with
     * @param quantity        the quantity of items to fetch on one page
     * @param page            the page number to fetch (group of [quantity] items)
     * @param orderBy         the SQL column to order the resources by
     * @param sortOrder       the sort order to sort the resources in
     *
     * @author Benjozork, hamza1311
     */
    suspend fun queryListing(request: RequestContext,
                             selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
                             quantity: Int,
                             page: Int,
                             orderBy: Column<*>,
                             sortOrder: SortOrder = SortOrder.ASC): Sr<Pair<List<R>, Boolean>>

    /**
     * Obtains an instance of [R] with a specific [id][UUID] ]in the database
     *
     * @param request the context of the [call][ApplicationCall] resulting in this operation, used for caching purposes
     * @param id      the [UUID] of the resource to fetch
     *
     * @author Benjozork, hamza1311
     */
    suspend fun get(request: RequestContext = FakeRequestContext, id: UUID): Sr<R>

    /**
     * Obtains an instance of [R] matching a specific SQL predicate
     *
     * @param request         the context of the [call][ApplicationCall] resulting in this operation, used for caching purposes
     * @param selectCondition the SQL predicate to match rows with
     *
     * @author Benjozork, hamza1311
     */
    suspend fun getOneMatching(request: RequestContext, selectCondition: SqlExpressionBuilder.() -> Op<Boolean>): Sr<R>

    /**
     * Adds an instance of [R] to the database
     *
     * @param res the resource to add
     *
     * @author Benjozork, hamza1311
     */
    suspend fun add(res: R): Sr<R>

    /**
     * Updates an instance of [R] in the database
     *
     * @param request the context of the [call][ApplicationCall] resulting in this operation, used for caching purposes
     * @param res     the resource to update
     * @param rawData a map of [property handles][PropMap.PropertyHandle.Ok] to replacement values. Can omit values to not update them.
     *
     * @author Benjozork, hamza1311
     */
    suspend fun update(request: RequestContext, res: R, rawData: Map<PropMap.PropertyHandle.Ok, Any?>): Sr<R>

    /**
     * Updates an instance of [R] in the database
     *
     * @param request the context of the [call][ApplicationCall] resulting in this operation, used for caching purposes
     * @param resource     the resource to update
     * @param data a map of [properties][KProperty1] to replacement values. Properties must resolve
     * to [Ok][PropMap.PropertyHandle.Ok] handles. Can omit values to not update them.
     *
     * @author Benjozork, hamza1311
     */
    suspend fun updateWithProperties(request: RequestContext, resource: R, data: Map<KProperty1<R, Any>, Any>): Sr<R>
            = update(request, resource, data.mapKeys { it.key.okHandle ?: error("update with a <KProperty1. Any> map cannot resolve to non-ok handles") })

    /**
     * Deletes an instance of [R] from the database
     *
     * @param res the resource to delete
     *
     * @author Benjozork, hamza1311
     */
    suspend fun delete(res: R): Sr<UUID>

    // Service exceptions

    open class Exception(causedBy: BException) : BException(causedBy) {

        class Fetching(causedBy: BException) : Exception(causedBy)

    }

}
