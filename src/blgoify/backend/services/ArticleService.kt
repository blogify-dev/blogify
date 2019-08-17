package blgoify.backend.services

import blgoify.backend.database.Articles
import blgoify.backend.database.Articles.toArticle
import blgoify.backend.database.Articles.uuid
import blgoify.backend.resources.Article
import blgoify.backend.services.models.Service
import blgoify.backend.util.query

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.util.*

object ArticleService : Service<Article> {

    override suspend fun getAll(): Set<Article> = query {
        Articles.selectAll().map { toArticle(it) }
    }.toSet()

    override suspend fun get(id: UUID ): Article? = query {
        Articles.select { uuid eq id }.mapNotNull { toArticle(it) }.singleOrNull()
    }

    override suspend fun add(res: Article) = query {
        Articles.insert { it[uuid] = res.uuid; it[title] = res.title; it[createdAt] = res.createdAt; }.run { Unit }
    }

    override suspend fun remove(id: UUID): Boolean {
        return false
    }

    override suspend fun update(res: Article): Boolean {
        return false
    }

}