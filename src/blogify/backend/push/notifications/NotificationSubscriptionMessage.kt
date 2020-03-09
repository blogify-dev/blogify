package blogify.backend.push.notifications

import blogify.backend.push.Message
import blogify.backend.push.PushServer

class SubscribeToNotifications(connection: PushServer.Connection, content: String)
    : Message.Incoming(connection, content) {

    init {
        error("ha !")
    }

}

class UnsubscribeToNotifications(connection: PushServer.Connection, content: String)
    : Message.Incoming(connection, content) {

    init {
        error("ho !")
    }

}
