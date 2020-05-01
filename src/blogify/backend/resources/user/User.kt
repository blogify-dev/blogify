package blogify.backend.resources.user

import blogify.reflect.annotations.Hidden
import blogify.backend.annotations.SqlTable
import blogify.reflect.annotations.Undisplayed
import blogify.reflect.annotations.search.NoSearch
import blogify.reflect.annotations.search.DelegatedSearchReceiver
import blogify.reflect.annotations.search.QueryByField
import blogify.reflect.annotations.search.SearchDefaultSort
import blogify.backend.annotations.maxByteSize
import blogify.backend.annotations.type
import blogify.backend.database.tables.Users
import blogify.backend.database.countReferredToBy
import blogify.backend.database.findReferredToBy
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventEmitter
import blogify.backend.events.models.EventTarget
import blogify.backend.events.models.EventType
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.push.Message
import blogify.reflect.computed.compound
import blogify.reflect.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticFile

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.*
import kotlin.random.Random

@SqlTable(Users::class)
data class User (
    @QueryByField
    @DelegatedSearchReceiver
    val username: String,

    @Hidden
    val password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !

    @QueryByField
    val name: String,

    val email: String,

    @NoSearch
    val profilePicture:
        @type("image/*")
        @maxByteSize(500_000)
        StaticFile,

    @NoSearch
    val coverPicture:
        @type("image/*")
        @maxByteSize(1_000_000)
        StaticFile,

    val isAdmin: Boolean = false,

    @Undisplayed
    @SearchDefaultSort
    val dsf: Int = Random.nextInt(),

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()

) : Resource(uuid), EventEmitter, EventTarget {

    inner class FollowedEvent(byUser: User) : Event(byUser, this, EventType.Notification) {
        val follower = byUser.uuid
        val followee = this@User.uuid
    }

    // Any notification that is about a user only has the user itself as a target
    @Hidden
    override val targets = setOf(this)

    override suspend fun sendEvent(appContext: ApplicationContext, event: Event) {
        GlobalScope.launch {
            appContext.pushServer.sendMessageToConnected(this@User, Message.Outgoing.Event(event))
        }
    }

    @Computed
    val followCount by compound {
        Users.uuid countReferredToBy Users.Follows.following
    }

    @Computed
    val followers by compound {
        Users.uuid findReferredToBy (Users.Follows.following to Users.Follows.follower)
    }

}
