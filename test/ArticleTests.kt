package me.benjozork

import blgoify.backend.mainModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication

import blgoify.backend.resources.Article

import kotlin.test.Test
import kotlin.test.assertEquals

import kotlinx.coroutines.runBlocking
import kotlin.test.assertTrue

class ArticleTests() {
    
    @Test
    fun `should provide all articles`() {

        val exampleData = mutableSetOf(
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

        withTestApplication({ mainModule(testing = true) }) {

            handleRequest(HttpMethod.Get, "api/articles/all") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                var receivedArticles: Array<Article>? = null

                assertEquals(HttpStatusCode.OK, response.status())

                runBlocking {
                   receivedArticles = jacksonObjectMapper().readValue(response.content!!)
                }

                assertTrue(exampleData.values.toTypedArray().contentEquals(receivedArticles!!))
            }

        }
    }

}