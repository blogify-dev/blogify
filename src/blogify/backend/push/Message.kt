package blogify.backend.push

import annotations.Invisible
import blogify.backend.resources.models.Resource
import reflect.models.Mapped
import reflect.sanitize
import reflect.slice
import blogify.backend.events.models.Event as ActualNotification

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import io.ktor.http.cio.websocket.Frame

private val objectMapper = jacksonObjectMapper().apply {
    registerModule(SimpleModule().apply { addSerializer(Resource.ResourceIdSerializer) })
}

/**
 * Represents a message sent from a connected client or to that client
 *
 * @author Benjozork
 */
sealed class Message : Mapped() {

    /**
     * Represents a message that is going out to the client
     *
     * @property frame the content of the frame to be sent for that message
     */
    abstract class Outgoing(message: String) : Message() {

        @Invisible val frame = Frame.Text(message)

        class Event(event: ActualNotification) : Outgoing(objectMapper.writeValueAsString(mapOf(
            "e" to event::class.qualifiedName,
            "t" to event.type,
            "d" to event.sanitize()
        )))

        class ActivityNotification(subject: Resource) : Outgoing(objectMapper.writeValueAsString(mapOf(
            "e" to "Activity${subject::class.simpleName!!.toUpperCase()}",
            "d" to subject.slice(setOf("article", "commenter"))
        )))
    }

    /**
     * Represents a message that is being sent by the client
     *
     * @property connection the [PushServer.Connection] from which the message originated
     */
    abstract class Incoming (
        @Invisible open val connection: PushServer.Connection
    ) : Message() {

        abstract suspend fun onArrival()

    }

}
