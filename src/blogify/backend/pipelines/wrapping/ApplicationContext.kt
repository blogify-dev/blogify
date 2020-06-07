package blogify.backend.pipelines.wrapping

import blogify.backend.appContext
import blogify.backend.database.models.QueryContext
import blogify.backend.database.persistence.models.DataStore
import blogify.backend.entity.Resource
import blogify.backend.database.persistence.models.Repository
import blogify.backend.push.PushServer
import blogify.backend.resources.reflect.construct
import blogify.backend.resources.reflect.extensions.sanitizeToString
import blogify.backend.resources.user.UserSettings
import blogify.backend.util.MapCache
import blogify.backend.util.parseJsonHandleMap
import blogify.reflect.entity.Entity
import blogify.reflect.models.Mapped

import com.fasterxml.jackson.databind.ObjectMapper

import epgx.types.Jsonb

import kotlinx.coroutines.runBlocking
import java.util.*

import kotlin.reflect.KClass

/**
 * Context object that describes the application context, including i.e. data stores and configuration.
 *
 * @author Benjozork
 */
class ApplicationContext (
    val dataStore: DataStore,
    val objectMapper: ObjectMapper
) {

    /**
     * Creates an implementation of [Jsonb.Converter] for [T], using [Mapped.sanitizeToString] for serialization
     * and [blogify.backend.resources.reflect.construct] for deserialization
     *
     * @author Benjozork
     */
    inline fun <reified T : Mapped> createJsonbConverter(): Jsonb.Converter<T> = object : Jsonb.Converter<T> {
        override fun serializer(instance: T) = instance.sanitizeToString()

        override fun deserializer(source: String): T = runBlocking {
            val params = source.parseJsonHandleMap(UserSettings::class).get()

            val queryContext = object : QueryContext {
                override val repositoryCache = MapCache<KClass<out Entity>, Repository<out Entity>>()

                override val entityCache = MapCache<UUID, Entity>()
            }

            T::class.construct (
                data = params,
                externalFetcher = { klass, id -> appContext.repository(klass).get(queryContext, id) }
            ).get()
        }
    }

    /**
     * Provides a [Repository] object for [TResource] using the data store in context
     *
     * @param TResource the type of [Resource] to return a repository for
     *
     * @author Benjozork
     */
    inline fun <reified TResource : Resource> repository(): Repository<TResource> {
        return this.dataStore.getRepository(TResource::class)
    }

    /**
     * Provides a [Repository] object for [TResource] using the data store in context
     *
     * @param TResource the type of [Resource] to return a repository for
     *
     * @author Benjozork
     */
    fun <TResource : Resource> repository(klass: KClass<TResource>): Repository<TResource> {
        return this.dataStore.getRepository(klass)
    }

    val pushServer = PushServer(this)

}
