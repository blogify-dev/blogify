package me.benjozork

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication

import me.benjozork.resources.Article

import kotlin.test.Test
import kotlin.test.assertEquals

import kotlinx.coroutines.runBlocking
import kotlin.test.assertTrue

class ArticleTests() {
    
    @Test
    fun `should provide all articles`() {

        val exampleData = mutableSetOf (
            Article(title = "Bonjour test"),
            Article(title = "Test test"),
            Article(title = "Test bonjour"),
            Article(title = "Bonjour bonjour"),
            Article(title = "Test test"),
            Article(title = "ABCDE 123"),
            Article(title = "123 ABCDE")
        ).associateBy { it.uuid }.toMutableMap()

        withTestApplication({ mainModule(testing = true) }) {

            val request = handleRequest(HttpMethod.Get, "api/articles/") {
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