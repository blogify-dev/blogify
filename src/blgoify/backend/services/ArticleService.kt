package blgoify.backend.services

import blgoify.backend.resources.Article
import blgoify.backend.services.models.Service

import java.util.*

object ArticleService : Service<Article> {

    private val exampleData = mutableSetOf(
        Article(title = "Bonjour test",    createdAt = 1565720325,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "Test test",       createdAt = 1565633925,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "Test bonjour",    createdAt = 1557685125,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "Bonjour bonjour", createdAt = 1565720332,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "Test test",       createdAt = 1555784325,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "ABCDE 123",       createdAt = 3133707525,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "ABCDE",           createdAt = 703793925,
                content = Article.Content("Lorem ipsum dolor sit amet.")),
        Article(title = "123 ABCDE",       createdAt = 70379392,
                content = Article.Content("Lorem ipsum dolor sit amet."))
    ).associateBy { it.uuid }.toMutableMap()

    override suspend fun getAll(): Set<Article> {
        return exampleData.values.toSet()
    }

    override suspend fun get(id: UUID ): Article? {
        return exampleData[id]
    }

    override suspend fun add(res: Article): Boolean {
        exampleData[res.uuid] = res
        return true
    }

    override suspend fun remove(id: UUID): Boolean {
        exampleData.remove(id) ?: return false
        return true
    }

    override suspend fun update(res: Article): Boolean {
        exampleData[res.uuid] = res
        return true
    }

}