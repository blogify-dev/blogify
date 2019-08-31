package blogify.backend.services.articles

import blogify.backend.database.Articles
import blogify.backend.database.Articles.uuid
import blogify.backend.resources.Article
import blogify.backend.services.handling.handleResourceDBDelete
import blogify.backend.services.handling.handleResourceDBFetch
import blogify.backend.services.handling.handleResourceDBFetchAll
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert

import java.util.UUID

object ArticleService : Service<Article> {

    override suspend fun getAll(): ResourceResultSet<Article> = handleResourceDBFetchAll(Articles)

    override suspend fun get(id: UUID): ResourceResult<Article> = handleResourceDBFetch(Articles, uuid, id)

    override suspend fun add(res: Article) = query {
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

        return@query res // So that we return the resource and not an insert statement
    }.mapError { e -> Service.Exception.Creating(e) } // Wrap possible error

    override suspend fun delete(id: UUID) = handleResourceDBDelete(Articles, uuid, id)

    override suspend fun update(res: Article): ResourceResult<Article> {
        TODO("not implemented !")
    }

}