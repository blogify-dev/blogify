package blogify.backend.resources.search

import blogify.backend.resources.Article
import blogify.backend.resources.User
import blogify.backend.services.UserService
import io.ktor.application.ApplicationCall
import java.util.*

/**
 * @author hamza1311
 */
data class Search<H> (
    val facet_counts: List<Any>?, // |\
    val found: Int?,              // | Will not appear on no results
    val hits: List<Hit<H>>?,         // |/
    val page: Int,
    val search_time_ms: Int
) {
    data class Hit<D>(
        val document: D,
        val highlights: List<Highlight>
    )

    data class ArticleDocument(
        val categories: List<String>,
        val content: String,
        val createdAt: Double,
        val createdBy: UUID,
        val summary: String,
        val title: String,
        val id: UUID
    ) {
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

    data class UserDocument(
        val username: String,
        val name: String,
        val email: String,
        val dsf_jank: Int,
        val id: UUID
    ) {
        suspend fun user(callContext: ApplicationCall): User = UserService.get(callContext, id).get()
    }

    data class Highlight(
        val `field`: String,
        val snippet: String
    )
}

fun Article.asDocument(): Search.ArticleDocument = Search.ArticleDocument(
    title = this.title,
    content = this.content,
    summary = this.summary,
    createdBy = this.createdBy.uuid,
    categories = this.categories.map { it.name },
    createdAt = this.createdAt.toDouble(),
    id = this.uuid
)