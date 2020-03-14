package blogify.backend.push

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.push.PushServer.ClosingCodes.BAD_FRAME
import blogify.backend.push.PushServer.ClosingCodes.INVALID_MESSAGE
import blogify.backend.push.notifications.SubscribeToNotifications
import blogify.backend.resources.User
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.doInstantiate
import blogify.backend.routing.closeAndExit
import blogify.backend.util.mappedByHandles
import blogify.backend.util.short
import blogify.backend.util.toDto

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSocketServerSession

import com.andreapivetta.kolor.yellow

import org.slf4j.LoggerFactory

/**
 * Class managing the notification / messaging push server.
 *
 * @author Benjozork
 */
class PushServer {

    /**
     * Represents a single connection to a [PushServer].
     *
     * @property user            the user that is using this connection
     * @property wsServerSession the [WebSocketServerSession] that is being used for communicaiton
     *
     * @author Benjozork
     */
    @Suppress("EXPERIMENTAL_API_USAGE")
    inner class Connection (
        val user: User,
        val wsServerSession: WebSocketServerSession
    ) {

        /**
         * Sends a [Message] through the connection
         */
        suspend fun send(message: Message.Outgoing) = wsServerSession.send(message.frame)

        /**
         * Start listening to and accepting incoming frames. Starts parsing incoming messages and instantiating message objects
         * when valid frames are received.
         *
         * **WARNING :** Do not block this function !
         */
        @Suppress("UNCHECKED_CAST")
        suspend fun startListening() {
            for (frame in wsServerSession.incoming) {
                if (frame !is Frame.Text)
                    wsServerSession.closeAndExit(BAD_FRAME)

                suspend fun close(reason: CloseReason): Nothing = wsServerSession.closeAndExit(reason)

                // Read and clean the frame text

                val cleanText = frame.readText().trim().dropWhile { it == '\"' }.dropLastWhile { it == '\"' }

                val textMatch = Regex("^(\\w+)\\s+(.*)$").matchEntire(cleanText)
                                ?: close(INVALID_MESSAGE("bad message syntax"))

                val prefix = textMatch.groupValues[1]
                val body = textMatch.groupValues[2]

                // Find the class it refers to using the prefix list

                val receivedClass = messagePrefixes.entries.firstOrNull { it.key == prefix }?.value
                                    ?: close(INVALID_MESSAGE("unknown command $prefix"))

                // Instantiate that class

                val boyDto = body
                    .toDto()
                    ?: close(INVALID_MESSAGE("body is not a DTO"))

                val bodyPayload = boyDto
                    .plus("connection" to this)
                    .mappedByHandles(receivedClass, unsafe = true)
                    ?: close(INVALID_MESSAGE("bad properties in body"))

                val receivedMessage = receivedClass.doInstantiate(bodyPayload).get()

                wsServerSession.send(Frame.Text(receivedMessage.toString()))
            }
        }

    }

    private val logger = LoggerFactory.getLogger("blogify-push-server")

    private val clientConnections = mutableMapOf<User, Connection>()

    private val messagePrefixes = mapOf (
        "subn" to SubscribeToNotifications::class
    )

    /**
     * Sends a message to all connected clients for a given user
     *
     * @param user the [User] for which to look for clients to send the message to
     */
    suspend fun sendMessageToConnected(user: User, message: Message.Outgoing) = this.clientConnections
        .filter { it.key eqr user }.values
        .forEach { it.send(message) }

    /**
     * Connects a new user client along with an associated [WebSocketServerSession]. Stores
     * a new instance of [Connection].
     *
     * @param user    the user that is connecting
     * @param session the [WebSocketServerSession] used to communicate
     */
    suspend fun connect(user: User, session: WebSocketServerSession) {
        this.clientConnections[user] = Connection(user, session).also { it.startListening() }
        logger.debug("client connection for ${user.uuid.short()} opened".yellow())
    }

    /**
     * Connects a user client with its associated [WebSocketServerSession]. Deletes the associated [Connection]
     * from the active connections.
     *
     * @param user    the user that is disconnecting
     * @param session the [WebSocketServerSession] that was used to communicate
     */
    fun disconnect(user: User, session: WebSocketServerSession) {
        val connection = clientConnections.entries.firstOrNull { it.value.wsServerSession == session }?.value ?: return
        clientConnections.remove(user, connection)

        logger.debug("client connection for ${user.uuid.short()} closed".yellow())
    }

    /**
     * Represents a response code to a WS frame
     */
    data class ResponseCode(val code: Short, val message: String)

    /**
     * Default frame response codes
     */
    object ResponseCodes {
         val OK = ResponseCode(4000, "OK")
         val AUTH_OK = ResponseCode(4001, "AUTH OK")
    }

    /**
     * Default frame closing / error codes
     */
    object ClosingCodes {
        val BAD_FRAME = CloseReason(4900, "invalid frame")

        val INVALID_TOKEN = CloseReason(4910, "bad token")

        fun INVALID_MESSAGE(prefix: String) = CloseReason(4020, "bad message '$prefix'")
    }

}
