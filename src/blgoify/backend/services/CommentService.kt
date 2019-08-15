package blgoify.backend.services

import blgoify.backend.resources.Article
import kotlinx.coroutines.runBlocking

import java.util.UUID

import blgoify.backend.resources.Comment
import blgoify.backend.resources.User
import blgoify.backend.services.models.Service

object CommentService : Service<Comment> {

    val articleService = ArticleService
    val userService    = UserService

    val tempArticle = runBlocking { articleService.getAll().toList()[0] }
    val tempUser    = runBlocking { userService.getAll().toList()[0]    }

    /*
    Temporary
     */
    private val exampleData = mutableSetOf(
        Comment(
            commenter = tempUser,
            article   = tempArticle,
            content   = "Comment"
        ),
        Comment(
            commenter = tempUser,
            article   = tempArticle,
            content   = "Comment"
        ),
        Comment(
            commenter = tempUser,
            article   = tempArticle,
            content   = "Comment"
        ),
        Comment(
            commenter = tempUser,
            article   = tempArticle,
            content   = "Comment"
        )
    ).associateBy { it.uuid }.toMutableMap()

    override suspend fun getAll(): Set<Comment> {
        return exampleData.values.toSet()
    }

    override suspend fun get(id: UUID): Comment? {
        return exampleData[id]
    }

    override suspend fun add(res: Comment): Boolean {
        exampleData[res.uuid] = res
        return true
    }

    override suspend fun remove(id: UUID): Boolean {
        exampleData.remove(id) ?: return false
        return true
    }

    override suspend fun update(res: Comment): Boolean {
        exampleData[res.uuid] = res
        return true
    }

    fun getForArticle(article: Article): List<Comment> {
        return exampleData.values.filter { it.article == article }
    }

    fun getByUser(user: User): List<Comment> {
        return exampleData.values.filter { it.commenter == user }
    }

}