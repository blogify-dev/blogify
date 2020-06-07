package blogify.backend.pipelines.wrapping

import blogify.backend.database.models.QueryContext
import blogify.backend.entity.Resource
import blogify.backend.persistence.models.Repository
import blogify.backend.util.MapCache
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.WrapBlocking
import blogify.reflect.entity.Entity

import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

import com.andreapivetta.kolor.green

import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope

import org.slf4j.LoggerFactory

import java.util.UUID

/**
 * Context object that wraps an [ApplicationCall] and server execution context information,
 * instead of exposing the whole [PipelineContext].
 *
 * This can include call caches, request IDs and more.
 *
 * @param enableCaching whether or not to enable L0 caching for this request context
 *
 * @property appContext     the [ApplicationContext] in which this request is executing
 * @property coroutineScope the [CoroutineScope] that is used for dispatching coroutines started in the request
 * @property call           the [ApplicationCall] that originated in the request
 *
 * @author Benjozork
 */
class RequestContext (
    val appContext: ApplicationContext,
    val coroutineScope: CoroutineScope,
    val call: ApplicationCall,
    enableCaching: Boolean = true
) : CoroutineScope by coroutineScope, QueryContext {

    override val entityCache: RequestCache<UUID, Entity> = RequestCache(enableCaching)

    override val repositoryCache = MapCache<KClass<out Entity>, Repository<out Entity>>()

    class RequestCache<K : Any, V : Any>(private val enableCaching: Boolean) : MapCache<K, V>() {

        private val logger = LoggerFactory.getLogger("blogify-request")

        override fun get(key: K): V? {
            return super.get(key).also {
                 if (it != null) logger.debug("used call cache for $key !".green())
            }
        }

        override fun <D : V> findOr(key: K, createNewValue: () -> D): Sr<D> {
            return if (enableCaching) super.findOr (
                key,
                { createNewValue().also { logger.debug("added $key to call cache".green()) } }
            ) else WrapBlocking { createNewValue() }
        }

        override suspend fun <D : V> findOrAsync(key: K, createNewValue: suspend () -> D): Sr<D> {
            return if (enableCaching) super.findOrAsync (
                key,
                { createNewValue().also { logger.debug("added $key to call cache".green()) } }
            ) else Wrap { createNewValue() }
        }

    }

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
        return this.appContext.dataStore.getRepository(TResource::class)
    }

    /**
     * Provides a [Repository] object for [TResource] using the data store in context
     *
     * @param TResource the type of [Resource] to return a repository for
     *
     * @author Benjozork
     */
    fun <TResource : Resource> repository(klass: KClass<TResource>): Repository<TResource> {
        return this.appContext.dataStore.getRepository(klass)
    }

}

/**
 * Represents a function executed in a [RequestContext]
 *
 * @author Benjozork
 */
typealias RequestContextFunction<TSubject> = suspend RequestContext.(TSubject) -> Unit
