package blogify.backend.database.tables

import blogify.common.util.Sr
import blogify.common.util.Wrap
import blogify.common.util.never
import reflectr.sanitize
import blogify.database.extensions.parentKey
import blogify.database.handling.query
import blogify.database.QueryContext
import blogify.backend.events.models.Event
import blogify.backend.resources.models.ResourceIdSerializer

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert

import epgx.models.PgTable
import epgx.types.JsonbColumnType

import com.github.kittinunf.result.coroutines.map
import reflectr.util.Dto

import java.time.Instant

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
@ExperimentalStdlibApi
object Events : PgTable("notifications") {

    private val dataConverter = object : JsonbColumnType.Converter<Dto> {

        private val objectMapper = jacksonObjectMapper().apply {
            val resourceModule = SimpleModule()
            resourceModule.addSerializer(ResourceIdSerializer)

            registerModule(resourceModule)
        }

        override fun serializer(instance: Dto) = objectMapper.writeValueAsString(instance)

        override fun deserializer(source: String) = objectMapper.readValue<Dto>(source)

    }

    val klass     = text      ("class")
    val timestamp = integer   ("timestamp")
    val emitter   = parentKey ("emitter", Users)
    val data      = jsonb     ("data", dataConverter)

    suspend fun convert(queryContext: QueryContext, source: ResultRow): Map<String, Any?> {
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
                    it[data] = event.sanitize()
                    it[emitter] = event.emitter.uuid
                    it[timestamp] = event.timestamp.epochSecond.toInt()
                    it[klass] = event::class.qualifiedName ?: never
                }
            }
        }.map { event }
    }

}
