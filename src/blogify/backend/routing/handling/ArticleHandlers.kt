package blogify.backend.routing.handling

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.auth.handling.authenticated
import blogify.backend.database.handling.query
import blogify.backend.database.handling.unwrappedQuery
import blogify.backend.database.tables.Articles
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.optionalParam
import blogify.backend.pipelines.pipelineError
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.Article
import blogify.backend.util.assertGet
import blogify.backend.util.reason
import blogify.reflect.sanitize
import blogify.reflect.slice

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

private val likes = Articles.Likes

@BlogifyDsl
val getUserDraftArticles: RequestContextFunction<Unit> = {
    authenticated { user ->
        val quantity = optionalParam(name = "quantity")?.toIntOrNull() ?: 25
        val page = optionalParam(name = "page")?.toIntOrNull() ?: 0

        val props = optionalParam("fields")?.split(",")?.toSet()

        val drafts = unwrappedQuery {
            Articles.obtainListing (
                queryContext = this,
                selectCondition = { (Articles.createdBy eq user.uuid) and (Articles.isDraft eq true) },
                quantity = quantity,
                page = page,
                orderBy = Articles.createdAt
            ).assertGet()
        }

        drafts.first.forEach { applyDefaultComputedPropertyResolver(it, user) }

        call.respond(object {
            val data = if (props == null)
                drafts.first.map { it.sanitize() }
            else
                drafts.first.map { it.slice(props) }
            val moreAvailable = drafts.second
        })
    }
}

/**
 * Request handler for fetching article like status
 */
@BlogifyDsl
val getArticleLikeStatus: RequestContextFunction<Unit> = {
    val id by queryUuid

    authenticated { user ->
        val liked = query {
            likes.select {
                (likes.article eq id) and (likes.user eq user.uuid)
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

    authenticated { user ->
        val isDraft = unwrappedQuery {
            Articles.slice(Articles.isDraft).select { Articles.uuid eq id }.single()
                .let { it[Articles.isDraft] }
        }

        if (isDraft)
            pipelineError(HttpStatusCode.NotFound, "")

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

    if (article.isDraft)
        pipelineError(HttpStatusCode.NotFound, "")

    authenticated({ it.isAdmin }) {
        Articles.update(article.copy(isPinned = !article.isPinned)).also {
            call.respond(HttpStatusCode.OK)
        }
    }
}
