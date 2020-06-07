package blogify.backend.routing

import blogify.backend.push.PushServer.ClosingCodes.INVALID_TOKEN
import blogify.backend.appContext
import blogify.backend.auth.jwt.validateJwt
import blogify.backend.database.models.QueryContext
import blogify.backend.persistence.models.Repository
import blogify.backend.push.PushServer
import blogify.backend.push.PushServer.ResponseCodes.AUTH_OK
import blogify.backend.entity.Resource
import blogify.backend.resources.user.User
import blogify.backend.util.MapCache
import blogify.backend.util.getOrNull

import com.andreapivetta.kolor.red

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.webSocket

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import kotlin.reflect.KClass

@Suppress("EXPERIMENTAL_API_USAGE")
fun Route.makePushServerRoutes() {

    route("/push") {

        webSocket("/connect") {

            var authenticatedUser: User? = null
            var connection: PushServer.Connection? = null

            incoming.consumeEach { frame ->
                if (frame !is Frame.Text)
                    return@consumeEach

                val queryContext = object : QueryContext {
                    override val repositoryCache = MapCache<KClass<out Resource>, Repository<out Resource>>()

                    override val entityCache = MapCache<UUID, Resource>()
                }

                if (authenticatedUser == null) {
                    authenticatedUser = validateJwt(queryContext, frame.readText().trim().replace("\"", "")).getOrNull()
                        ?: closeAndExit(INVALID_TOKEN)

                    connection = appContext.pushServer.connect(authenticatedUser ?: error("fatal: authenticatedUser is null".red()), this)

                    send(AUTH_OK)
                } else {
                    connection!!.readFrame(frame)
                }
            }

            closeReason.invokeOnCompletion {
                 appContext.pushServer.disconnect(authenticatedUser ?: error("fatal: authenticatedUser is null".red()), this)
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
