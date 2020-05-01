package blogify.backend.database.tables

import blogify.backend.appContext
import blogify.backend.database.extensions.keyOf
import blogify.backend.database.extensions.nullableKeyOf
import blogify.backend.database.extensions.strongKey
import blogify.backend.database.extensions.weaKey
import blogify.backend.database.handling.query
import blogify.backend.database.models.ResourceTable
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.user.User
import blogify.backend.util.Sr
import blogify.backend.util.Wrap

import org.jetbrains.exposed.sql.*

import com.github.kittinunf.result.coroutines.SuspendableResult

object Comments : ResourceTable.UserCreated<Comment>() {

    val commenter     =  strongKey ("commenter", Users)
    val article       =  strongKey ("article", Articles)
    val content       =  text      ("content")
    val parentComment =  weaKey    ("parent_comment", Comments)
    val createdAt     =  integer   ("created_at")

    override val authorColumn = commenter

    object Likes: Table("comment_likes") {

        val user    = this.uuid("user") keyOf Users
        val comment = this.uuid("comment") keyOf Comments

        override val primaryKey = PrimaryKey(
            user,
            comment
        )
    }

    override suspend fun insert(resource: Comment): Sr<Comment> {
        return Wrap {
            query {
                this.insert {
                    it[uuid] = resource.uuid
                    it[commenter] = resource.commenter.uuid
                    it[article] = resource.article.uuid
                    it[content] = resource.content
                    it[parentComment] = resource.parentComment?.uuid
                    it[createdAt] = resource.createdAt
                }
            }
            return@Wrap resource
        }
    }

    override suspend fun update(resource: Comment): Boolean {
        return query {
            this.update(where = { Users.uuid eq resource.uuid }) {
                it[uuid] = resource.uuid
                it[commenter] = resource.commenter.uuid
                it[article] = resource.article.uuid
                it[content] = resource.content
                it[parentComment] = resource.parentComment?.uuid
            }
        }.get() == 1
    }

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) =
        SuspendableResult.of<Comment, Repository.Exception.Fetching> {
            Comment(
                uuid = source[uuid],
                content = source[content],
                article = appContext.repository<Article>()
                    .get(requestContext, source[article]).get(),
                commenter = appContext.repository<User>()
                    .get(requestContext, source[commenter]).get(),
                parentComment = source[parentComment]?.let {
                    appContext.repository<Comment>()
                        .get(requestContext, it).get()
                },
                createdAt = source[createdAt]
            )
        }

}
