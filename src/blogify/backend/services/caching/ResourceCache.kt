package blogify.backend.services.caching

import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.Resource.ObjectResolver.FakeApplicationCall
import blogify.backend.services.models.ResourceResult

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
val ApplicationCall.cache: MutableMap<UUID, Resource>
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

suspend fun <R : Resource> ApplicationCall.cachedOrElse(id: UUID, fetcher: suspend () -> ResourceResult<R>): ResourceResult<R> {
    if (this is FakeApplicationCall) { // Since a FakeApplicationCall does not have anything in it, do not try to access the cache
        return fetcher()
    }

    val cached = this.cache[id]

    return if (cached == null) {
        val fetchResult = fetcher()

        if (fetchResult is SuspendableResult.Success) {
            this.cache[id] = fetchResult.get()
            fetchResult
        } else {
            fetchResult
        }
    } else {
        logger.debug("used cache for $id !".green())
        SuspendableResult.of { cached as R }
    }
}