package blgoify.backend.services.articles

import blgoify.backend.database.Articles
import blgoify.backend.database.Articles.uuid
import blgoify.backend.resources.Article
import blgoify.backend.services.handling.handleResourceDBFetch
import blgoify.backend.services.handling.handleResourceDBFetchAll
import blgoify.backend.services.models.ResourceResult
import blgoify.backend.services.models.ResourceResultSet
import blgoify.backend.services.models.Service
import blgoify.backend.util.booleanReturnQuery

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

import java.util.UUID

object ArticleService : Service<Article> {

    override suspend fun getAll(): ResourceResultSet<Article> = handleResourceDBFetchAll(Articles)

    override suspend fun get(id: UUID): ResourceResult<Article> = handleResourceDBFetch(Articles, uuid, id)

    override suspend fun add(res: Article) = booleanReturnQuery {
        Articles.insert {
            it[uuid]       = res.uuid
            it[title]      = res.title
            it[createdAt]  = res.createdAt
            it[createdBy]  = res.createdBy.uuid
            it[categories] = res.categories.joinToString(separator = ",")
        }

        val content = res.content ?: error("content not captured on article serialize")

        Articles.Content.insert {
            it[text]    = content.text
            it[summary] = content.summary
            it[article] = res.uuid
        }
    }

    override suspend fun remove(id: UUID) = booleanReturnQuery {
        Articles.deleteWhere { uuid eq id }
    }

    override suspend fun update(res: Article): Boolean {
        return false
    }

}