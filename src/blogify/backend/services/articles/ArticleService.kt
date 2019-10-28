package blogify.backend.services.articles

import blogify.backend.database.Articles
import blogify.backend.database.Articles.uuid
import blogify.backend.resources.Article
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ArticleService : Service<Article>(Articles) {

    override suspend fun add(res: Article) = query {
        Articles.insert {
            it[uuid]      = res.uuid
            it[title]     = res.title
            it[createdAt] = res.createdAt
            it[createdBy] = res.createdBy.uuid
            it[content]   = res.content
            it[summary]   = res.summary
        }

        val cats = res.categories

        for (cat in cats) {
            Articles.Categories.insert {
                it[name] = cat.name
                it[article] = res.uuid
            }
        }

        transaction {
            val statement = TransactionManager.current().connection.createStatement()
            val query = """
                UPDATE articles
                SET doc = to_tsvector(articles.content)
                WHERE uuid = '${res.uuid}';
            """.trimIndent()
            println(query)
            statement.execute(query)
        }

        return@query res // So that we return the resource and not an insert statement
    }.mapError { e -> Exception.Creating(e) } // Wrap possible error

    override suspend fun update(res: Article): ResourceResult<*> = query {
        Articles.update(where = { uuid eq res.uuid }) {
            it[title]   = res.title
            it[content] = res.content
            it[summary] = res.summary
        }

        val cats = res.categories

        Articles.Categories.deleteWhere { Articles.Categories.article eq res.uuid }

        cats.forEach { cat ->
            Articles.Categories.update {
                it[name] = cat.name
            }
        }
    }.mapError { e -> Exception.Updating(e) }

}
