package blogify.backend.database.tables

import krate.extensions.parentKey
import krate.extensions.weaKey
import krate.EntityTable
import blogify.backend.resources.Comment

import org.jetbrains.exposed.sql.*

@ExperimentalStdlibApi
object Comments : EntityTable<Comment>() {

    val commenter     =  parentKey ("commenter", Users)
    val article       =  parentKey ("article", Articles)
    val content       =  text      ("content")
    val parentComment =  weaKey    ("parent_comment", Comments)
    val createdAt     =  integer   ("created_at")

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
