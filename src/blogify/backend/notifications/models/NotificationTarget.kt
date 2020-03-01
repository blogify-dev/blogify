package blogify.backend.notifications.models

/**
 * Applies to objects that receive [notifications][Notification] when a [NotificationSource] object
 * they are related to generates one.
 *
 * @author Benjozork
 */
public interface NotificationTarget {

    fun sendNotification(notification: Notification<*, *, *>)

}
