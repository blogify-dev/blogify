package blogify.backend.events.models

import reflectify.models.Identified

/**
 * Applies to objects that generate [events][Event] when an [EventEmitter] interacts with them.
 *
 * @property targets defines what targets receive events when an [EventEmitter] interacts with this object
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
interface EventSource : Identified {

    /**
     * The [targets][EventTarget] that are targeted by this event
     */
    val targets: Set<EventTarget>

}
