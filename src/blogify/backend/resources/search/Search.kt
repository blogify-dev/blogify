package blogify.backend.resources.search

import blogify.backend.resources.Article
import blogify.backend.resources.User
import blogify.backend.services.UserService
import io.ktor.application.ApplicationCall
import java.util.*

/**
 * Models for deserializing json returned by typesense
 *
 * @author hamza1311
 */
data class Search<H> (
    val facet_counts: List<Any>?, // |\
    val found: Int?,              // | Will not appear on no results
    val hits: List<Hit<H>>?,      // |/
    val page: Int,
    val search_time_ms: Int
) {
    data class Hit<D>(
        val document: D,
        val highlights: List<Highlight>
    )

    /**
     * Model representing an [article][Article] hit returned by typesense
     */
    data class ArticleDocument(
        val categories: List<String>,
        val content: String,
        val createdAt: Double,
        val createdBy: UUID,
        val summary: String,
        val title: String,
        val id: UUID
    ) {

        /**
         * Convert [ArticleDocument] to [Article].
         * It constructs the [article][Article] object using the properties of the given [document][ArticleDocument]
         * It does **NOT** makes a database call
         *
         * @return The article object created by properties of the given [document][ArticleDocument]
         */
        suspend fun article(): Article = Article(
            title = title,
            content = content,
            summary = summary,
            createdBy = UserService.get(id = createdBy).get(),
            categories = categories.map { Article.Category(it) },
            createdAt = createdAt.toLong(),
            uuid = id
        )
    }

    /**
     *  Model representing an [user][User] hit returned by typesense
     *
     *  @param dsf_jank This is a workaround for `default_sorting_field` parameter in typesense, which is a required parameter whose value can only be a `float` or `int32`. Its value is always `0` in our case
     */
    data class UserDocument(
        val username: String,
        val name: String,
        val email: String,
        val dsf_jank: Int,
        val id: UUID
    ) {

        /**
         * Convert [UserDocument] to [User].
         * It constructs the [user][User] object by fetcting user with uuid of [id] from [users][blogify.backend.database.Users] table
         * This is a database call
         *
         * @return The user object with uuid of [id]
         */
        suspend fun user(callContext: ApplicationCall): User = UserService.get(callContext, id).get()
    }

    data class Highlight(
        val `field`: String,
        val snippet: String
    )
}

/**
 * Constructs [Search.ArticleDocument] from [Article]
 */
fun Article.asDocument(): Search.ArticleDocument = Search.ArticleDocument(
    title = this.title,
    content = this.content,
    summary = this.summary,
    createdBy = this.createdBy.uuid,
    categories = this.categories.map { it.name },
    createdAt = this.createdAt.toDouble(),
    id = this.uuid
)

/**
 * Constructs [Search.UserDocument] from [User]
 */
fun User.asDocument(): Search.UserDocument = Search.UserDocument(
    username = this.username,
    name = this.name,
    email = this.email,
    dsf_jank = 0,
    id = this.uuid
)