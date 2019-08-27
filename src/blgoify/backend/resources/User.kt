package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.annotation.JsonIgnore

import blgoify.backend.resources.models.Resource

import java.util.*

@JsonIdentityInfo (
    scope     = User::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class User (
    val name:     String,
    val username: String,

    @JsonIgnore var password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid)