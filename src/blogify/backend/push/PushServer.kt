package blogify.backend.push

import blogify.common.util.short
import blogify.common.util.concurrentMapOf
import blogify.common.util.getOr
import reflectify.propMap
import reflectify.models.extensions.ok
import reflectify.entity.instantiation.construct
import reflectify.entity.mappedByHandles
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.push.PushServer.ClosingCodes.INVALID_MESSAGE
import blogify.backend.push.notifications.SubscribeToNotifications
import blogify.backend.resources.user.User
import blogify.backend.routing.closeAndExit
import blogify.backend.util.*

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.WebSocketServerSession

import kotlinx.coroutines.launch

import org.slf4j.LoggerFactory

import com.andreapivetta.kolor.yellow
import com.andreapivetta.kolor.red
import com.github.kittinunf.result.coroutines.getOrNull

import kotlin.reflect.KClass

/**
 * Class managing the notification / messaging push server.
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
class PushServer(val appContext: ApplicationContext) {

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
        val wsServerSession: WebSocketServerSession,
        val appContext: ApplicationContext
    ) {

        /**
         * Sends a [Message] through the connection
         */
        suspend fun send(message: Message.Outgoing) = wsServerSession.send(message.frame)

        /**
         * Accepts an incoming frame and instantiates a [Message] object
         */
        @ExperimentalStdlibApi
        @Suppress("UNCHECKED_CAST")
        suspend fun readFrame(frame: Frame.Text) {

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

            // Make an actual object

            val boyDto = body
                .toDto()
                ?: close(INVALID_MESSAGE("body is not a DTO"))

            val bodyPayload = boyDto
                .plus("connection" to this)
                .mappedByHandles(receivedClass, unsafe = true)
                .getOrNull() ?: close(INVALID_MESSAGE("bad properties in body"))

            // Instantiate that class

            val noConnectionHandleMessage = "fatal: no connection property on class ${receivedClass.simpleName}".red()

            val receivedMessage = receivedClass.construct (
                objectMapper = appContext.objectMapper,
                data = bodyPayload,
                externallyProvided = setOf(receivedClass.propMap.ok["connection"] ?: error(noConnectionHandleMessage))
            ).getOr {
                it.printStackTrace()
                close(INVALID_MESSAGE("couldn't instantiate message - ${it.javaClass.simpleName}: ${it.message}"))
            }

            wsServerSession.launch { receivedMessage.onArrival() }

            wsServerSession.send(Frame.Text(receivedMessage.toString()))
        }

    }

    private val logger = LoggerFactory.getLogger("blogify-push-server")

    private val clientConnections = concurrentMapOf<User, Connection>()

    val messagePrefixes = concurrentMapOf<String, KClass<out Message.Incoming>>()

    init {
        messagePrefixes["subn"] = SubscribeToNotifications::class
    }

    /**
     * Sends a message to all connected clients for a given user
     */
    suspend fun sendMessageToAllConnected(message: Message.Outgoing) = this.clientConnections.values
        .forEach { it.send(message) }

    /**
     * Sends a message to all connected clients for a given user
     *
     * @param user the [User] for which to look for clients to send the message to
     */
    suspend fun sendMessageToConnected(user: User, message: Message.Outgoing) = this.clientConnections
        .filter { it.key == user }.values
        .forEach { it.send(message) }

    /**
     * Connects a new user client along with an associated [WebSocketServerSession]. Stores and returns
     * a new instance of [Connection].
     *
     * @param user    the user that is connecting
     * @param session the [WebSocketServerSession] used to communicate
     */
    fun connect(user: User, session: WebSocketServerSession): Connection {
        val connection = Connection(user, session, appContext)
        this.clientConnections[user] = connection

        logger.debug("client connection for ${user.uuid.short()} opened".yellow())
        return connection
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

        @Suppress("FunctionName")
        fun INVALID_MESSAGE(prefix: String) = CloseReason(4020, "bad message '$prefix'")
    }

}
