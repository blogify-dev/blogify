package blogify.backend.push

import blogify.backend.resources.reflect.sanitize
import blogify.backend.notifications.models.Notification as ActualNotification

import io.ktor.http.cio.websocket.Frame

/**
 * Represents a message sent from a connected client or to that client
 *
 * @author Benjozork
 */
sealed class Message {

    /**
     * Represents a message that is going out to the client
     *
     * @property frame the content of the frame to be sent for that message
     */
    abstract class Outgoing(message: String) : Message() {

        val frame = Frame.Text(message)

        class Notification(notification: ActualNotification<*, *, *>) : Outgoing("notif ${notification.sanitize()}")

    }

    /**
     * Represents a message that is being sent by the client
     *
     * @property connection the [PushServer.Connection] from which the message originated
     * @property content    the raw text content of the message
     */
    abstract class Incoming(val connection: PushServer.Connection, val content: String) : Message()

}
