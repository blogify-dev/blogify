package blogify.backend.resources.search

import blogify.backend.resources.Article
import blogify.backend.services.UserService
import java.util.*

/**
 * @author hamza1311
 */
data class Search(
    val facet_counts: List<Any>,
    val found: Int,
    val hits: List<Hit>,
    val page: Int,
    val search_time_ms: Int
) {
    data class Hit(
        val document: Document,
        val highlights: List<Highlight>
    )

    data class Document(
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

    data class Highlight(
        val `field`: String,
        val snippet: String
    )
}

fun Article.asDocument(): Search.Document = Search.Document(
    title = this.title,
    content = this.content,
    summary = this.summary,
    createdBy = this.createdBy.uuid,
    categories = this.categories.map { it.name },
    createdAt = this.createdAt.toDouble(),
    id = this.uuid
)