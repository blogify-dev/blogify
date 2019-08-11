package me.benjozork.services

import me.benjozork.resources.Article
import me.benjozork.services.models.Service

import java.util.*

class ArticleService : Service<Article> {

    private val exampleData = mutableSetOf (
        Article(title = "Bonjour test"),
        Article(title = "Test test"),
        Article(title = "Test bonjour"),
        Article(title = "Bonjour bonjour"),
        Article(title = "Test test"),
        Article(title = "ABCDE 123"),
        Article(title = "123 ABCDE")
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