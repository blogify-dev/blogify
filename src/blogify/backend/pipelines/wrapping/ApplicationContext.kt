package blogify.backend.pipelines.wrapping

import blogify.backend.persistence.models.DataStore
import blogify.backend.resources.models.Resource
import blogify.backend.persistence.models.Repository
import blogify.backend.push.PushServer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import epgx.types.Jsonb

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
     * Creates an implementation of [Jsonb.Converter] for [T]
     *
     * @author Benjozork
     */
    inline fun <reified T> jsonbConverter(): Jsonb.Converter<T> = object : Jsonb.Converter<T> {
        override fun serializer(instance: T): String = objectMapper.writeValueAsString(instance)
        override fun deserializer(source: String): T = objectMapper.readValue(source)
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
