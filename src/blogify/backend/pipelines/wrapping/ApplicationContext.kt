package blogify.backend.pipelines.wrapping

import blogify.backend.persistence.models.DataStore
import blogify.backend.resources.models.Resource
import blogify.backend.persistence.models.Repository
import blogify.backend.resources.User
import blogify.backend.resources.models.eqr
import blogify.backend.util.short

import com.andreapivetta.kolor.yellow

import io.ktor.http.cio.websocket.Frame

import kotlinx.coroutines.channels.SendChannel

import org.slf4j.LoggerFactory

import kotlin.reflect.KClass

/**
 * Context object that describes the application context, including i.e. data stores and configuration.
 *
 * @author Benjozork
 */
class ApplicationContext (
    val dataStore: DataStore
) {

    /**
     * Class managing the notification / messaging push server
     */
    class PushServer {

        private val logger = LoggerFactory.getLogger("blogify-push-server")

        private val clientConnections = mutableMapOf<User, SendChannel<Frame>>()

        /**
         * Sends a message to all connected clients for a given user
         *
         * @param user the [User] for which to look for clients to send the message to
         */
        suspend fun sendToConnected(user: User, data: String) = this.clientConnections
            .filter { it.key eqr user }.values
            .forEach { it.send(Frame.Text(data)) }

        /**
         * Connects a new user client along with an associated [SendChannel]
         */
        fun connect(user: User, channel: SendChannel<Frame>) {
            this.clientConnections[user] = channel
            logger.debug("client connection for ${user.uuid.short()} opened".yellow())
        }

        /**
         * Connects a user client with its associated [SendChannel]
         */
        fun disconnect(user: User, channel: SendChannel<Frame>) {
            this.clientConnections.remove(user, channel)
            logger.debug("client connection for ${user.uuid.short()} closed".yellow())
        }

    }

    /**
     * Provides a [Repository] object for [TResource] using the data store in context
     *
     * @param TResource the type of [Resource] to return a repository for
     *
     * @author Benjozork
     */
    inline fun <reified TResource : Resource> repository(): Repository<TResource> {
        return this.dataStore.getRepository(TResource::class)
    }

    /**
     * Provides a [Repository] object for [TResource] using the data store in context
     *
     * @param TResource the type of [Resource] to return a repository for
     *
     * @author Benjozork
     */
    fun <TResource : Resource> repository(klass: KClass<TResource>): Repository<TResource> {
        return this.dataStore.getRepository(klass)
    }

    val pushServer = PushServer()

}
