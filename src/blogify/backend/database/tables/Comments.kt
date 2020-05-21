package blogify.backend.database.tables

import blogify.backend.database.extensions.parentKey
import blogify.backend.database.extensions.weaKey
import blogify.backend.database.models.ResourceTable
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.Article
import blogify.backend.resources.Comment

import org.jetbrains.exposed.sql.*

import com.github.kittinunf.result.coroutines.SuspendableResult

object Comments : ResourceTable.UserCreated<Comment>() {

    val commenter     =  parentKey ("commenter", Users)
    val article       =  parentKey ("article", Articles)
    val content       =  text      ("content")
    val parentComment =  weaKey    ("parent_comment", Comments)
    val createdAt     =  integer   ("created_at")

    override val authorColumn = commenter

    init {
        bind (uuid, Comment::uuid)
        bind (commenter, Comment::commenter)
        bind (article, Comment::article)
        bind (content, Comment::content)
        bind (parentComment, Comment::parentComment)
        bind (createdAt, Comment::createdAt)
    }

    object Likes : Table("comment_likes") {

        val user    = parentKey ("user", Users)
        val comment = parentKey ("comment", Comments)

        override val primaryKey = PrimaryKey(user, comment)
    }

}
