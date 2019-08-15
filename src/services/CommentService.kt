package me.benjozork.services

import me.benjozork.resources.Comment
import me.benjozork.services.models.Service
import java.util.UUID

object CommentService : Service<Comment> {
    private val exampleData = mutableSetOf(
        Comment(
            commenter = UUID.fromString("13ecf550-ea25-48ee-83ae-371d17391a0a"),
            article = UUID.fromString("894231ae-7d23-4843-955e-7e3e1b315014"),
            content = "Comment"
        ),
        Comment(
            commenter = UUID.fromString("3fd07048-c22d-4428-9d82-4ed3c1d4fd12"),
            article = UUID.fromString("0f5e3807-3037-4708-9020-4cf296e9faad"),
            content = "Comment2"
        ),
        Comment(
            commenter = UUID.fromString("13ecf550-ea25-48ee-83ae-371d17391a0a"),
            article = UUID.fromString("894231ae-7d23-4843-955e-7e3e1b315014"),
            content = "Comment3"
        ),
        Comment(
            commenter = UUID.fromString("3fd07048-c22d-4428-9d82-4ed3c1d4fd12"),
            article = UUID.fromString("8f5a920f-ebb8-47e9-b8b8-ad41208f1940"),
            content = "Comment4"
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

    fun getForArticle(articleUUID: UUID): List<Comment> {
        return exampleData.values.filter { it.article == articleUUID }
    }

    fun getByUser(userUUID: UUID): List<Comment> {
        return exampleData.values.filter { it.commenter == userUUID }
    }

}