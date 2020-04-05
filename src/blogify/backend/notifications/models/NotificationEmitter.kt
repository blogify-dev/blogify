package blogify.backend.notifications.models

import blogify.backend.resources.models.Identified

/**
 * Applies to objects that can generate a [Notification] when they interact with a
 * [NotificationSource].
 *
 * @author Benjozork
 */
interface NotificationEmitter : Identified
