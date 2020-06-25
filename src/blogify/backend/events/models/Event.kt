package blogify.backend.events.models

import blogify.common.util.assertGet
import reflectify.models.Mapped
import blogify.backend.database.tables.Events
import blogify.backend.pipelines.wrapping.RequestContext

import java.time.Instant

@ExperimentalStdlibApi
open class Event (
    val emitter: EventEmitter,
    val source: EventSource,
    val type: EventType
) : Mapped() {

    val timestamp: Instant = Instant.now()

    @ExperimentalStdlibApi
    suspend fun send(request: RequestContext) {
        source.targets.forEach { it.sendEvent(request.appContext, this) }

        Events.insert(this).assertGet()
    }

}
