package blogify.backend.push.notifications

import blogify.backend.push.Message
import blogify.backend.push.PushServer

// override is temporary while we fix mapper to look at superclass props
class SubscribeToNotifications(val test: String, override val connection: PushServer.Connection)
    : Message.Incoming(connection) {

    init {
        error("ha !")
    }

}

class UnsubscribeToNotifications(connection: PushServer.Connection)
    : Message.Incoming(connection) {

    init {
        error("ho !")
    }

}
