package blogify.backend.events.models

import blogify.backend.database.tables.Events
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.reflect.models.Mapped
import blogify.backend.util.assertGet

import java.time.Instant

open class Event (
    val emitter: EventEmitter,
    val source: EventSource,
    val type: EventType
) : Mapped() {

    val timestamp: Instant = Instant.now()

    suspend fun send(request: RequestContext) {
        source.targets.forEach { it.sendEvent(request.appContext, this) }

        Events.insert(this).assertGet()
    }

}
