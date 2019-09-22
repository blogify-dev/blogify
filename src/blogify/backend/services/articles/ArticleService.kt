package blogify.backend.services.articles

import blogify.backend.database.Articles
import blogify.backend.database.Articles.Content.article
import blogify.backend.database.Articles.uuid
import blogify.backend.resources.Article
import blogify.backend.services.handling.deleteWithIdInTable
import blogify.backend.services.handling.fetchWithIdFromTable
import blogify.backend.services.handling.fetchAllFromTable
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

import java.util.UUID

object ArticleService : Service<Article> {

    override suspend fun getAll(): ResourceResultSet<Article> = fetchAllFromTable(Articles)

    override suspend fun get(id: UUID): ResourceResult<Article> = fetchWithIdFromTable(Articles, uuid, id)

    override suspend fun add(res: Article) = query {
        Articles.insert {
            it[uuid]       = res.uuid
            it[title]      = res.title
            it[createdAt]  = res.createdAt
            it[createdBy]  = res.createdBy.uuid
        }

        val content = res.content ?: error("content not captured on article serialize")

        Articles.Content.insert {
            it[text]    = content.text
            it[summary] = content.summary
            it[article] = res.uuid
        }
        val cats = res.categories ?: error("category not captured on article serialize")

        for (cat in cats) {
            Articles.Categories.insert {
                it[name] = cat.name
                it[article] = res.uuid
            }
        }

        return@query res // So that we return the resource and not an insert statement
    }.mapError { e -> Service.Exception.Creating(e) } // Wrap possible error

    override suspend fun delete(id: UUID) = deleteWithIdInTable(Articles, uuid, id)

    override suspend fun update(res: Article): ResourceResult<Article> =
        query {
            Articles.update({ uuid eq res.uuid }) {
                it[title] = res.title
            }

            val content = res.content ?: error("content not captured on article serialize")

            Articles.Content.update({ article eq res.uuid }) {
                it[text] = content.text
                it[summary] = content.summary
            }

            val cats = res.categories

            Articles.Categories.deleteWhere { Articles.Categories.article eq res.uuid }
            for (cat in cats) {
                Articles.Categories.update {
                    it[name] = cat.name
                }
            }
            return@query res
        }.mapError { e -> Service.Exception.Updating(e) }

}