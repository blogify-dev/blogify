package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blgoify.backend.resources.models.Resource
import blgoify.backend.util.hash
import java.util.*

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "uuid")
data class User (
    val name: String,
    val username: String,
    var password: String,
    var temporaryToken: String? = null,
    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {
    fun hashPassword(): User {
        val user = this
        user.password = user.password.hash()
        return user
    }

    fun applyToken(token: String): User {
        val user = this
        user.temporaryToken = token
        return user
    }

    fun hidePassword(): User {
        val user = this
        user.password = "<hidden>"
        return user
    }
}