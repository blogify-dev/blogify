package blogify.backend.routing.handling

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.auth.handling.autenticated
import blogify.backend.database.handling.query
import blogify.backend.database.tables.Articles
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.Article
import blogify.backend.util.assertGet
import blogify.backend.util.reason

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

private val likes = Articles.Likes

/**
 * Request handler for fetching article like status
 */
@BlogifyDsl
val getArticleLikeStatus: RequestContextFunction<Unit> = {
    val id by queryUuid

    autenticated { subject ->
        val liked = query {
            likes.select {
                (likes.article eq id) and (likes.user eq subject.uuid)
            }.count()
        }.assertGet() == 1L

        call.respond(liked)
    }
}

/**
 * Request handler for flipping article like status
 */
@BlogifyDsl
val flipArticleLike: RequestContextFunction<Unit> = {
    val id by queryUuid

    autenticated { user ->
        // Figure whether the article was already liked by the user
        val alreadyLiked = query {
            likes.select {
                (likes.article eq id) and (likes.user eq user.uuid)
            }.count()
        }.assertGet() == 1L

        if (!alreadyLiked) { // Add a like if none were present
            query {
                likes.insert {
                    it[likes.article] = id
                    it[likes.user]    = user.uuid
                }
            }.assertGet()

            call.respond(HttpStatusCode.OK, reason("comment liked"))
        } else { // Remove an existing like if there was one
            query {
                likes.deleteWhere {
                    (likes.article eq id) and (likes.user eq user.uuid)
                }
            }.assertGet()

            call.respond(HttpStatusCode.OK, reason("comment unliked"))
        }
    }
}

/**
 * Request handler for flipping article pin status
 */
@BlogifyDsl
val flipArticlePin: RequestContextFunction<Unit> = {
    val id by queryUuid
    val article = obtainResource<Article>(id)

    autenticated(predicate = { it.isAdmin }) {
        Articles.update(article.copy(isPinned = !article.isPinned)).also {
            call.respond(HttpStatusCode.OK)
        }
    }
}
