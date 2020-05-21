package blogify.backend.database.tables

import blogify.backend.appContext
import blogify.backend.database.extensions.parentKey
import blogify.backend.database.extensions.strongKey
import blogify.backend.database.handling.unwrappedQuery
import blogify.backend.database.models.ResourceTable
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.Article
import blogify.backend.resources.user.User
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.asBoolean
import blogify.backend.util.asResult

import org.jetbrains.exposed.sql.*

import com.github.kittinunf.result.coroutines.map

object Articles : ResourceTable.UserCreated<Article>() {

    val title      = text      ("title")
    val createdAt  = integer   ("created_at")
    val createdBy  = strongKey ("created_by", Users)
    val content    = text      ("content")
    val summary    = text      ("summary")
    val isDraft    = bool      ("is_draft").default(false)
    val isPinned   = bool      ("is_pinned").default(false)

    override val authorColumn = createdBy

    init {
        bind (uuid,      Article::uuid)
        bind (title,     Article::title)
        bind (createdAt, Article::createdAt)
        bind (createdBy, Article::createdBy)
        bind (content,   Article::content)
        bind (summary,   Article::summary)
        bind (isDraft,   Article::isDraft)
        bind (isPinned,  Article::isPinned)
    }

    override suspend fun insert(resource: Article): Sr<Article> = Wrap {
        super.insert(resource).get()

        unwrappedQuery {
            Categories.batchInsert(resource.categories) {
                this[Categories.article] = resource.uuid
                this[Categories.name] = it.name
            }
        }
    }.map { resource }

    override suspend fun update(resource: Article): Boolean = Wrap {
        super.update(resource).asResult()

        unwrappedQuery {
            Categories.deleteWhere { Categories.article eq resource.uuid } == 1
        }

        unwrappedQuery {
            Categories.batchInsert(resource.categories) {
                this[Categories.article] = resource.uuid
                this[Categories.name] = it.name
            }
        }
    }.asBoolean()

    override suspend fun delete(resource: Article) = Wrap {
        super.delete(resource).asResult().get()

        unwrappedQuery {
            Categories.deleteWhere { Categories.article eq resource.uuid } == 1
        }
    }.asBoolean()

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) = Wrap {
        Article (
            uuid = source[uuid],
            title = source[title],
            createdAt = source[createdAt],
            createdBy = appContext.repository<User>()
                .get(requestContext, source[createdBy]).get(),
            content = source[content],
            summary = source[summary],
            isDraft = source[isDraft],
            isPinned = source[isPinned],
            categories = unwrappedQuery {
                Categories.select { Categories.article eq source[uuid] }.toList()
            }.map { Categories.convert(it) }
        )
    }

    object Categories : Table() {

        val article = parentKey("article", Articles)
        val name    = text("name")

        override val primaryKey = PrimaryKey(article, name)

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) =
            Article.Category(
                name = source[name]
            )

    }

    object Likes: Table("article_likes") {

        val user    = parentKey("user", Users)
        val article = parentKey("article", Articles)

        override val primaryKey = PrimaryKey(
            user,
            article
        )

    }

}
