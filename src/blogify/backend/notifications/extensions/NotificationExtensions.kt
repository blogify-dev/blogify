package blogify.backend.notifications.extensions

import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.notifications.models.Notification

/**
 * Sends a notification to all of its targets
 */
suspend fun Notification<*, *, *>.send(appContext: ApplicationContext) = this.targets.forEach { it.sendNotification(appContext, this) }
