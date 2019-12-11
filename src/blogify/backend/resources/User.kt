package blogify.backend.resources

import blogify.backend.annotations.search.NoSearch
import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.annotations.Invisible
import blogify.backend.annotations.search.DelegatedSearchReceiver
import blogify.backend.annotations.type

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import java.util.*

@JsonIdentityInfo (
    scope     = User::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class User (
    @DelegatedSearchReceiver
    val username: String,

    @Invisible
    val password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !

    val name: String,

    val email: String,

    @NoSearch
    val profilePicture: @type("image/*") StaticResourceHandle,

    @NoSearch
    val coverPicture: @type("image/*") StaticResourceHandle,

    @Invisible
    val isAdmin: Boolean = false,

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid)
