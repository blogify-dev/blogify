package blogify.backend.push.notifications

import blogify.backend.push.Message
import blogify.backend.push.PushServer

// override is temporary while we fix mapper to look at superclass props
@ExperimentalStdlibApi
class SubscribeToNotifications(val test: String, override val connection: PushServer.Connection)
    : Message.Incoming(connection) {

    override suspend fun onArrival() {
        TODO("Not yet implemented")
    }

}

@ExperimentalStdlibApi
class UnsubscribeToNotifications(connection: PushServer.Connection)
    : Message.Incoming(connection) {

    override suspend fun onArrival() {
        TODO("Not yet implemented")
    }

}
