package blogify.backend.events.models

import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.reflect.models.Identified

/**
 * Applies to objects that receive [events][Event] when an [EventSource] object
 * they are related to generates one.
 *
 * @author Benjozork
 */
interface EventTarget : Identified {

    /**
     * Function used to send a event to the [EventTarget]
     *
     * @param appContext the [ApplicationContext] in which the notification was sent
     * @param event      the [Event] to send
     */
    suspend fun sendEvent(appContext: ApplicationContext, event: Event)

}
