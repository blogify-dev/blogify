package blogify.backend.events.models

import reflectify.models.Identified

/**
 * Applies to objects that can generate an [Event] when they interact with an [EventSource].
 *
 * @author Benjozork
 */
interface EventEmitter : Identified
