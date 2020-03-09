package blogify.backend.notifications.models

import blogify.backend.resources.reflect.models.Mapped

/**
 * Represents a notification
 *
 * @property emitter the [NotificationEmitter] object that made the action that caused this notificaiton
 * @property source  the [NotificationSource] object the notification is about
 * @property targets the [targets][NotificationTarget] of the notification
 *
 * @author Benjozork
 */
data class Notification <
        TEmitter : NotificationEmitter,
        TSource : NotificationSource,
        TTarget : NotificationTarget > (

    val emitter: TEmitter,
    val source:  TSource,
    val targets: Set<TTarget>

) : Mapped()
