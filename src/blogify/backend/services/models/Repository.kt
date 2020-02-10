package blogify.backend.services.models

import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.Resource.ObjectResolver.FakeApplicationCall
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.util.BException
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.SrList

import io.ktor.application.ApplicationCall

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder

import java.util.*

/**
 * Service interface for fetching, creating, updating and deleting [resources][Resource].
 */
interface Repository<R : Resource> {

    /**
     * Obtains all instances of [R] in the database
     *
     * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
     *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
     *
     * @param limit the max number of items to fetch. Defaults to 256.
     *
     * @return a [SrList] of [R] items
     *
     * @author Benjozork, hamza1311
     */
    suspend fun getAll(callContext: ApplicationCall = FakeApplicationCall, limit: Int = 256): SrList<R>

    /**
     * Obtains an instance of [R] with a specific [id][UUID] ]in the database
     *
     * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
     *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
     *
     * @param id the [UUID] of the resource to fetch
     *
     * @return a [Wrap] of an [R] item with the provided [id]
     *
     * @author Benjozork, hamza1311
     */
    suspend fun get(callContext: ApplicationCall = FakeApplicationCall, id: UUID): Sr<R>

    /**
     * Obtains a set of instances of [R] matching a given [predicate]
     *
     * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
     *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
     *
     * @param predicate an Exposed predicate that is used to return the needed items
     *
     * @return a [SrList] of [R] items matching [predicate]
     *
     * @author hamza1311
     */
    suspend fun getMatching(callContext: ApplicationCall = FakeApplicationCall, predicate: SqlExpressionBuilder.() -> Op<Boolean>): SrList<R>

    suspend fun add(res: R): Sr<R>

    suspend fun update(res: R, rawData: Map<PropMap.PropertyHandle.Ok, Any?>): Sr<R>

    /**
     * Deletes an instance of [R] from the database
     *
     * @param res the resource to delete
     *
     * @return a [Wrap] of the [UUID] of the deleted resource
     *
     * @author Benjozork, hamza1311
     */
    suspend fun delete(res: R): Sr<UUID>

    // Service exceptions

    open class Exception(causedBy: BException) : BException(causedBy) {

        class Fetching(causedBy: BException) : Exception(causedBy)

    }

}