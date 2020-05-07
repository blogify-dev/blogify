package blogify.backend.push.notifications

import blogify.backend.database.tables.Articles
import blogify.backend.push.Message
import blogify.backend.push.PushServer
import blogify.backend.resources.Article

// override is temporary while we fix mapper to look at superclass props
class SubscribeToNotifications(val test: String, override val connection: PushServer.Connection)
    : Message.Incoming(connection) {

    override suspend fun onArrival() {
        TODO("Not yet implemented")
    }

}

class UnsubscribeToNotifications(connection: PushServer.Connection)
    : Message.Incoming(connection) {

    override suspend fun onArrival() {
        TODO("Not yet implemented")
    }

}
