package blogify.backend.notifications.models

import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.models.Identified

/**
 * Applies to objects that receive [notifications][Notification] when a [NotificationSource] object
 * they are related to generates one.
 *
 * @author Benjozork
 */
public interface NotificationTarget : Identified {

    fun registerNotification(appContext: ApplicationContext, notification: Notification<*, *, *>) {

    }

    /**
     * Function used to send a notification to the [NotificationTarget]
     *
     * @param appContext   the [ApplicationContext] in which the notification was sent
     * @param notification the [Notification] to send
     */
    suspend fun sendNotification(appContext: ApplicationContext, notification: Notification<*, *, *>)

}
