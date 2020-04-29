package blogify.backend.database.tables

import blogify.backend.database.extensions.keyOf
import blogify.backend.database.handling.query
import blogify.backend.events.models.Event
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource
import blogify.reflect.sanitize
import blogify.backend.util.*

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert

import epgx.models.PgTable
import epgx.types.Jsonb

import com.github.kittinunf.result.coroutines.map

import java.time.Instant

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
object Events : PgTable("notifications") {

    private val dataConverter = object : Jsonb.Converter<Dto> {

        private val objectMapper = jacksonObjectMapper().apply {
            val resourceModule = SimpleModule()
            resourceModule.addSerializer(Resource.ResourceIdSerializer)

            registerModule(resourceModule)
        }

        override fun serializer(instance: Dto) = objectMapper.writeValueAsString(instance)

        override fun deserializer(source: String) = objectMapper.readValue<Dto>(source)

    }

    val klass     = text    ("class")
    val timestamp = integer ("timestamp")
    val emitter   = uuid    ("emitter") keyOf Users
    val data      = jsonb   ("data", dataConverter)

    suspend fun convert(request: RequestContext, source: ResultRow): Map<String, Any?> {
        return mutableMapOf (
            "data"      to source[data],
            "emitter"   to source[emitter],
            "timestamp" to Instant.ofEpochSecond(source[timestamp].toLong()),
            "klass"     to source[klass]
        ).toMap()
    }

    suspend fun insert(event: Event): Sr<Event> {
        return Wrap {
            query {
                insert {
                    it[data]      = event.sanitize()
                    it[emitter]   = event.emitter.uuid
                    it[timestamp] = event.timestamp.epochSecond.toInt()
                    it[klass]     = event::class.qualifiedName ?: never
                }
            }
        }.map { event }
    }

}
