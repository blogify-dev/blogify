package blogify.backend.events.models

import blogify.backend.database.Notifications
import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.sanitize
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

import java.time.Instant

open class Event (
    val emitter: EventEmitter,
    val source: EventSource,
    val type: EventType
) : Mapped() {

    val timestamp: Instant = Instant.now()

    suspend fun send(request: RequestContext) {
        source.targets.forEach { it.sendEvent(request.appContext, this) }
        if (type == EventType.Notification) {
            println("Reached fucking here")
            query {
                println("inside query")
                Notifications.insert {
                    println("inserting...")
                    it[this.data] = this@Event.sanitize()
                    it[this.emitter] = this@Event.emitter.uuid
                    it[this.timestamp] = this@Event.timestamp.epochSecond.toInt()
                }.also {
                    println("ins")
                    println(it)
                }

                Notifications.selectAll().map { Notifications.convert<Event>(request, it) }.toList().also { println("-----it---------"); println(it) }
            }
        }
    }

}
