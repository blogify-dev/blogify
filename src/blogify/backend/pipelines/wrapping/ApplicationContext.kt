package blogify.backend.pipelines.wrapping

import reflectr.models.Mapped
import reflectr.entity.Entity
import krate.DatabaseContext
import krate.QueryContext
import krate.persistence.models.Repository
import krate.extensions.repository
import reflectr.entity.instantiation.construct
import blogify.backend.appContext
import blogify.backend.config.DatabaseConfig
import blogify.backend.config.TypesenseConfig
import blogify.backend.config.loadConfig
import blogify.backend.push.PushServer
import blogify.backend.resources.reflect.extensions.sanitizeToString
import blogify.backend.resources.user.UserSettings
import blogify.backend.util.parseJsonHandleMap

import com.fasterxml.jackson.databind.ObjectMapper

import epgx.types.JsonbColumnType

import kotlinx.coroutines.runBlocking
import java.util.*
import krate.util.MapCache
import kotlin.reflect.KClass

/**
 * Context object that describes the application context, including i.e. data stores and configuration.
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
class ApplicationContext(val objectMapper: ObjectMapper) : DatabaseContext {

    override val repoCache = MapCache<KClass<out Entity>, Repository<Entity>>()

    val databaseConfig = loadConfig("db", DatabaseConfig.serializer())
    val typesenseConfig = loadConfig("ts", TypesenseConfig.serializer())

    /**
     * Creates an implementation of [JsonbColumnType.Converter] for [T], using [Mapped.sanitizeToString] for serialization
     * and [reflectr.entity.instantiation.construct] for deserialization
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
