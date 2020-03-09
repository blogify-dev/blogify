package blogify.backend.resources

import blogify.backend.annotations.Invisible
import blogify.backend.annotations.SqlTable
import blogify.backend.annotations.Undisplayed
import blogify.backend.annotations.search.NoSearch
import blogify.backend.annotations.search.DelegatedSearchReceiver
import blogify.backend.annotations.search.QueryByField
import blogify.backend.annotations.search.SearchDefaultSort
import blogify.backend.annotations.maxByteSize
import blogify.backend.annotations.type
import blogify.backend.database.Users
import blogify.backend.database.countReferredToBy
import blogify.backend.database.findReferredToBy
import blogify.backend.notifications.models.Notification
import blogify.backend.notifications.models.NotificationEmitter
import blogify.backend.notifications.models.NotificationTarget
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.computed.compound
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticResourceHandle

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import java.util.*
import kotlin.random.Random

@JsonIdentityInfo (
    scope     = User::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
@SqlTable(Users::class)
data class User (
    @QueryByField
    @DelegatedSearchReceiver
    val username: String,

    @Invisible
    val password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !

    @QueryByField
    val name: String,

    val email: String,

    @NoSearch
    val profilePicture:
        @type("image/*")
        @maxByteSize(500_000)
        StaticResourceHandle,

    @NoSearch
    val coverPicture:
        @type("image/*")
        @maxByteSize(1_000_000)
        StaticResourceHandle,

    @Invisible
    val isAdmin: Boolean = false,

    @Undisplayed
    @SearchDefaultSort
    val dsf: Int = Random.nextInt(),

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()

) : Resource(uuid), NotificationEmitter, NotificationTarget {

    // Any notification that is about a user only has the user itself as a target
    @Invisible
    override val targets = setOf(this)

    override suspend fun sendNotification(appContext: ApplicationContext, notification: Notification<*, *, *>) {
        appContext.pushServer.sendToConnected(this, "Hello, ws notif test")
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
