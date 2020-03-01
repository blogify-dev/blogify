package blogify.backend.notifications.extensions

import blogify.backend.notifications.models.Notification

/**
 * Sends a notification to all of its targets
 */
fun Notification<*, *, *>.send() = this.targets.forEach { it.sendNotification(this) }
