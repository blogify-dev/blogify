package me.benjozork.services

import me.benjozork.resources.Article
import me.benjozork.services.models.Service
import java.util.*

class ArticleService : Service<Article> {

    private val exampleData = mutableSetOf(
        Article(title = "Bonjour test", createdAt = 1565720325),
        Article(title = "Test test", createdAt = 1565633925),
        Article(title = "Test bonjour", createdAt = 1557685125),
        Article(title = "Bonjour bonjour", createdAt = 1565720332),
        Article(title = "Test test", createdAt = 1555784325),
        Article(title = "ABCDE 123", createdAt = 3133707525),
        Article(title = "ABCDE", createdAt = 703793925),
        Article(title = "123 ABCDE", createdAt = 703793925)
    ).associateBy { it.uuid }.toMutableMap()

    override fun getAll(): Set<Article> {
        return exampleData.values.toSet()
    }

    override fun get(id: UUID): Article? {
        return exampleData[id]
    }

    override fun add(res: Article): Boolean {
        exampleData[res.uuid] = res
        return true
    }

    override fun remove(id: UUID): Boolean {
        exampleData.remove(id) ?: return false
        return true
    }

    override fun update(res: Article): Boolean {
        exampleData[res.uuid] = res
        return true
    }
}