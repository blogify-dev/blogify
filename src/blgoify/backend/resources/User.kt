package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blgoify.backend.resources.models.Resource

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "uuid")
data class User (
    val name: String,
    val firebaseUid: String
) : Resource()