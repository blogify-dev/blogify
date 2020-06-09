package blogify.backend.pipelines.wrapping

import blogify.common.util.MapCache
import blogify.reflect.models.Mapped
import blogify.reflect.entity.Entity
import blogify.reflect.entity.database.DatabaseContext
import blogify.reflect.entity.database.QueryContext
import blogify.reflect.entity.database.persistence.models.Repository
import blogify.reflect.entity.database.extensions.repository
import blogify.reflect.entity.instantiation.construct
import blogify.backend.appContext
import blogify.backend.push.PushServer
import blogify.backend.resources.reflect.extensions.sanitizeToString
import blogify.backend.resources.user.UserSettings
import blogify.backend.util.parseJsonHandleMap

import com.fasterxml.jackson.databind.ObjectMapper

import epgx.types.JsonbColumnType

import kotlinx.coroutines.runBlocking
import java.util.*

import kotlin.reflect.KClass

/**
 * Context object that describes the application context, including i.e. data stores and configuration.
 *
 * @author Benjozork
 */
class ApplicationContext(val objectMapper: ObjectMapper) : DatabaseContext {

    override val repoCache = MapCache<KClass<out Entity>, Repository<Entity>>()

    /**
     * Creates an implementation of [JsonbColumnType.Converter] for [T], using [Mapped.sanitizeToString] for serialization
     * and [blogify.reflect.entity.instantiation.construct] for deserialization
     *
     * @author Benjozork
     */
    inline fun <reified T : Mapped> createJsonbConverter(): JsonbColumnType.Converter<T> = object : JsonbColumnType.Converter<T> {
        override fun serializer(instance: T) = instance.sanitizeToString()

        override fun deserializer(source: String): T = runBlocking {
            val params = source.parseJsonHandleMap(UserSettings::class).get()

            val queryContext = object : QueryContext {
                override val databaseContext: DatabaseContext get() = this@ApplicationContext

                override val objectMapper get() = appContext.objectMapper

                override val entityCache = MapCache<UUID, Entity>()
            }

            T::class.construct (
                objectMapper = objectMapper,
                data = params,
                externalFetcher = { klass, id -> appContext.repository(klass).get(queryContext, id) }
            ).get()
        }
    }

    val pushServer = PushServer(this)

}
