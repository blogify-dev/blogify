package blogify.backend.notifications.extensions

import blogify.backend.notifications.models.Notification
import blogify.backend.notifications.models.NotificationEmitter
import blogify.backend.notifications.models.NotificationSource

/**
 * Spawns a [blogify.backend.notifications.models.Notification] about a [TSource]
 */
fun <TSource : NotificationSource, TEmitter : NotificationEmitter>
        TSource.spawnNotification(emitter: TEmitter) {

    val notification = Notification(emitter, this, this.targets)

}
