package blogify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.annotation.JsonIgnore

import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.resources.models.Resource
import blogify.backend.services.models.Service
import blogify.backend.util.foldForOne

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.select

import java.util.*

@JsonIdentityInfo (
    scope     = User::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class User (
    val username: String,

    @JsonIgnore val password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

    data class PersonalInformation(val name: String, val email: String)

    suspend fun info(): SuspendableResult<PersonalInformation, Service.Exception.Fetching> = query {
        Users.UserInfo.select { Users.UserInfo.user eq uuid }.foldForOne (
            one      = { Users.UserInfo.convert(it) },
            multiple = { error("multiple entries") },
            none     = { error("no entries") }
        ).get()
    }.mapError { e -> Service.Exception.Fetching(e) }

}