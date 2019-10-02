package blogify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blogify.backend.resources.models.Resource
import blogify.backend.util.noslice

import java.util.*

@JsonIdentityInfo (
    scope     = User::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class User (
    val username: String,

    @noslice val password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !

    val name: String,

    val email: String,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid)