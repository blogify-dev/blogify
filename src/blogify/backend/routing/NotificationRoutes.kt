package blogify.backend.routing

import blogify.backend.push.PushServer.ClosingCodes.BAD_FRAME
import blogify.backend.push.PushServer.ClosingCodes.INVALID_TOKEN
import blogify.backend.appContext
import blogify.backend.auth.jwt.validateJwt
import blogify.backend.push.PushServer
import blogify.backend.push.PushServer.ResponseCodes.AUTH_OK
import blogify.backend.resources.User
import blogify.backend.util.getOrNull

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.webSocket

import kotlinx.coroutines.CancellationException

fun Route.makePushServerRoutes() {

    route("/push") {

        webSocket("/connect") {

            // Attempt to initialize the connection

            val givenUser: User

            val firstFrame = incoming.receive()

            if (firstFrame is Frame.Text) {
                val x = firstFrame.readText()
                givenUser = validateJwt(token = firstFrame.readText().trim().replace("\"", "").also { println(it) }).getOrNull()
                            ?: closeAndExit(INVALID_TOKEN)
                send(AUTH_OK)
                appContext.pushServer.connect(givenUser, this)
            } else {
                closeAndExit(BAD_FRAME)
            }

            // Connection is closing

            closeReason.invokeOnCompletion {
                 appContext.pushServer.disconnect(givenUser, this)
            }

        }

    }

}

suspend fun WebSocketServerSession.send(responseCode: PushServer.ResponseCode) =
    send(Frame.Text("${responseCode.code} - ${responseCode.message}"))

suspend fun WebSocketServerSession.closeAndExit(closeReason: CloseReason): Nothing {
    close(closeReason)
    throw CancellationException("ws connection closed")
}
