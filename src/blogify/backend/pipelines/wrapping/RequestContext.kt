package blogify.backend.pipelines.wrapping

import blogify.backend.resources.models.Resource
import blogify.backend.services.models.Repository
import blogify.backend.util.MapCache
import blogify.backend.util.Sr

import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

import kotlinx.coroutines.CoroutineScope

import java.util.UUID

/**
 * Context object that wraps an [ApplicationCall] and server execution context information,
 * instead of exposing the whole [PipelineContext].
 *
 * This can include call caches, request IDs and more.
 *
 * @property applicationContext the [ApplicationContext] in which this request is executing
 * @property coroutineScope     the [CoroutineScope] that is used for dispatching coroutines started in the request
 * @property call               the [ApplicationCall] that originated in the request
 *
 * @author Benjozork
 */
class RequestContext (
    val applicationContext: ApplicationContext,
    val coroutineScope: CoroutineScope,
    val call: ApplicationCall,
    val resourceCache: MapCache<UUID, Sr<Resource>>
) : CoroutineScope by coroutineScope {

    /**
     * Executes a [RequestContextFunction] in the context of this [RequestContext].
     *
     * A reference to the context is given, along with the provided subject.
     *
     * @param function the [RequestContextFunction] to execute
     * @param subject  the subject to be passed to [function]
     *
     * @author Benjozork
     */
    suspend fun <TSubject> execute(function: RequestContextFunction<TSubject>, subject: TSubject) {
        function(this, subject)
    }

    /**
     * Provides a [Repository] object for [TResource] using the data store in context
     *
     * @param TResource the type of [Resource] to return a repository for
     *
     * @author Benjozork
     */
    inline fun <reified TResource : Resource> repository(): Repository<TResource> {
        return this.applicationContext.dataStore.getRepository(TResource::class)
    }

}

/**
 * Represents a function executed in a [RequestContext]
 *
 * @author Benjozork
 */
typealias RequestContextFunction<TSubject> = suspend RequestContext.(TSubject) -> Unit
