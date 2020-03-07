package blogify.backend.notifications.extensions

import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.notifications.models.Notification
import blogify.backend.notifications.models.NotificationEmitter
import blogify.backend.notifications.models.NotificationSource
import blogify.backend.util.short

import com.andreapivetta.kolor.magenta

import org.slf4j.LoggerFactory

private val notificationsLogger = LoggerFactory.getLogger("blogify-notifications")

/**
 * Spawns a [blogify.backend.notifications.models.Notification] about a [TSource]
 *
 * @receiver the subject of the notification. For example, for a top-level comment of an article, the article would
 *           be the subject.
 *
 * @param appContext the [ApplicationContext] in which the notification was created
 * @param emitter    the [NotificationEmitter] responsible for the notification
 *
 * @author Benjozork
 */
suspend fun <TSource : NotificationSource, TEmitter : NotificationEmitter>
        TSource.spawnNotification(appContext: ApplicationContext, emitter: TEmitter) {

    val notification = Notification(emitter, this, this.targets)

    notificationsLogger.debug("new notification about ${this.uuid.short()} (${this::class.simpleName}) from ${emitter.uuid.short()} (${emitter::class.simpleName})".magenta())

    notification.send(appContext)

}
