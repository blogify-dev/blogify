package blogify.backend.database.tables

import blogify.backend.database.extensions.jsonb
import blogify.backend.database.extensions.keyOf
import blogify.backend.database.handling.query
import blogify.backend.events.models.Event
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.reflect.sanitize
import blogify.backend.util.*

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert

import com.github.kittinunf.result.coroutines.map

import java.time.Instant

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
object Events : Table("notifications") {

    val klass     = text    ("class")
    val timestamp = integer ("timestamp")
    val emitter   = uuid    ("emitter") keyOf Users
    val data      = jsonb   ("data")

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
