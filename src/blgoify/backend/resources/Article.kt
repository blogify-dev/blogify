package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.*
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blgoify.backend.resources.models.Resource
import blgoify.backend.services.UserService

import kotlinx.coroutines.runBlocking

import java.util.*

/**
 * Represents an Article [Resource].
 *
 * @property title     The title of the [Article].
 * @property createdAt The time of creation of the [Article], in `UNIX` timestamp format.
 * @property content   The [Content][Article.Content] of the article. Not included in the JSON serialization.

 */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "uuid")
data class Article (
    val title:     String,
    val createdAt: Long = Date().time,

    @JsonIdentityReference(alwaysAsId = true)
    val createdBy: User =  runBlocking { UserService.getAll().toList()[0] } /* VERY TEMPORARY */,

    @JsonProperty(access = WRITE_ONLY)
    val content: Content,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

    /**
     * Represents the content of an [Article].
     *
     * @property text    The text content of the article.
     * @property summary The summary of the content.
     */
    data class Content(val text: String, val summary: String)

}