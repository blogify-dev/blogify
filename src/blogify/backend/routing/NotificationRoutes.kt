package blogify.backend.routing

import blogify.backend.appContext
import blogify.backend.notifications.extensions.spawnNotification
import blogify.backend.resources.User
import blogify.backend.util.toUUID

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.webSocket

fun Route.makeNotificationRoutes() {

    route("/notifications") {

        webSocket("/ws") {

            var givenUser: User? = null

            for (frame in incoming) {
                if (givenUser == null) {
                    frame as Frame.Text

                    val uuid = frame.readText().toUUID()

                    givenUser = appContext.repository<User>().get(id = uuid).get()

                    appContext.pushServer.connect(givenUser, outgoing)

                    givenUser.spawnNotification(appContext, givenUser)
                }
            }

            closeReason.invokeOnCompletion {
                givenUser?.let { user -> appContext.pushServer.disconnect(user, outgoing) }
            }

        }

    }

}
