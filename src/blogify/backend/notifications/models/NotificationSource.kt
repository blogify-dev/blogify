package blogify.backend.notifications.models

/**
 * Applies to objects that generate [notifications][Notification] when a [NotificationEmitter]
 * interacts with them.
 *
 * @property targets defines what users receive notifications when a [NotificationEmitter]
 *                   interacts with this object
 *
 * @author Benjozork
 */
public interface NotificationSource {

    /**
     * The [targets][NotificationTarget] that are targeted by this notification
     */
    val targets: Set<NotificationTarget>

}
