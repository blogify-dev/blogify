package blogify.backend.notifications.models

import blogify.backend.resources.models.Identified

/**
 * Applies to objects that can generate a [Notification] when they interact with a
 * [NotificationSource].
 *
 * @author Benjozork
 */
public interface NotificationEmitter : Identified
