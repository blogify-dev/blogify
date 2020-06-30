package blogify.backend.database.tables

import krate.extensions.parentKey
import krate.extensions.strongKey
import krate.EntityTable
import blogify.backend.resources.Article

import org.jetbrains.exposed.sql.*

@ExperimentalStdlibApi
object Articles : EntityTable<Article>() {

    val title      = text      ("title")
    val createdAt  = integer   ("created_at")
    val createdBy  = strongKey ("created_by", Users)
    val content    = text      ("content")
    val summary    = text      ("summary")
    val isDraft    = bool      ("is_draft").default(false)
    val isPinned   = bool      ("is_pinned").default(false)
    val isHidden   = bool      ("is_shitpost").default(false)

    init {
        bind (uuid,      Article::uuid)
        bind (title,     Article::title)
        bind (createdAt, Article::createdAt)
        bind (createdBy, Article::createdBy)
        bind (content,   Article::content)
        bind (summary,   Article::summary)
        bind (isDraft,   Article::isDraft)
        bind (isPinned,  Article::isPinned)
        bind (isHidden,  Article::isHidden)

        bind (Categories, Article::categories,
            conversionFunction = { row ->
                Article.Category(row[Categories.name])
            },
            insertionFunction = { article, category, insert ->
                insert[Categories.article] = article.uuid
                insert[Categories.name] = category.name
            }
        )
    }

    object Categories : Table() {

        val article = parentKey("article", Articles)
        val name    = text("name")

        override val primaryKey = PrimaryKey(article, name)

    }

    object Likes: Table("article_likes") {

        val user    = parentKey("user", Users)
        val article = parentKey("article", Articles)

        override val primaryKey = PrimaryKey(user, article)

    }

}
