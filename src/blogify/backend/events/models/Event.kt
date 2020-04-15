package blogify.backend.events.models

import blogify.backend.database.Notifications
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.util.getOrNull
import blogify.backend.util.never

import java.time.Instant

open class Event (
    val emitter: EventEmitter,
    val source: EventSource,
    val type: EventType
) : Mapped() {

    val timestamp: Instant = Instant.now()

    suspend fun send(request: RequestContext) {
        source.targets.forEach { it.sendEvent(request.appContext, this) }

        Notifications.insert(this).getOrNull() ?: never
    }

}
