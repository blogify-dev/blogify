package blogify.backend.pipelines.caching

import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.Resource.ObjectResolver.FakeApplicationCall
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.short

import io.ktor.application.ApplicationCall
import io.ktor.util.AttributeKey

import com.andreapivetta.kolor.green

import com.github.kittinunf.result.coroutines.SuspendableResult

import org.slf4j.LoggerFactory

import java.util.UUID

private val logger = LoggerFactory.getLogger("blogify-resource-cache")

/**
 * The [AttributeKey] used for identifying the [Resource] cache.
 */
private val ResourceCacheKey = AttributeKey<MutableMap<UUID, Resource>>("blogify-ResourceCache")

/**
 * Gives access to an [ApplicationCall]'s [Resource] cache.
 */
private val ApplicationCall.cache: MutableMap<UUID, Resource>
    get() {
        return try {
            this.attributes[ResourceCacheKey]
        } catch (e: Exception) {
            this.createResourceCache()
            this.attributes[ResourceCacheKey]
        }
    }

/**
 * Creates a [Resource] cache on an [ApplicationCall].
 */
private fun ApplicationCall.createResourceCache() {
     this.attributes.put(ResourceCacheKey, mutableMapOf())
}

/**
 * Tries to fetched a cached version of a [Resource] of type [R] and UUID [id] from the [ApplicationCall],
 * and caches the result of a fetcher function if it misses.
 *
 * @receiver the [ApplicationCall] on which to perform the lookup
 *
 * @param id      the [UUID] for which to perform the lookup
 * @param fetcher the function that would fetch the resource if there is a cache miss
 *
 * @return an [Wrap] of the requested resource
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
suspend fun <R : Resource> ApplicationCall.cachedOrElse(id: UUID, fetcher: suspend () -> Sr<R>): Sr<R> {
    if (this is FakeApplicationCall) { // Since a FakeApplicationCall does not have anything in it, do not try to access the cache
        return fetcher()
    }

    val cached = this.cache[id]

    return if (cached == null) {
        fetcher().also {
            if (it is SuspendableResult.Success) { // Add to cache if it's successfully fetched
                this.cache[id] = it.get()
                logger.debug("added ${id.short()} to call cache".green())
            }
        }
    } else {
        logger.debug("used cache for ${id.short()} !".green())
        Wrap { cached as R }
    }
}
