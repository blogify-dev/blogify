package blogify.backend.database.tables

import blogify.backend.appContext
import blogify.backend.database.extensions.keyOf
import blogify.backend.database.handling.query
import blogify.backend.database.models.ResourceTable
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.Article
import blogify.backend.resources.user.User
import blogify.backend.util.Sr
import blogify.backend.util.Wrap

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import com.github.kittinunf.result.coroutines.SuspendableResult

object Articles : ResourceTable.UserCreated<Article>() {

    val title      = varchar ("title", 512)
    val createdAt  = integer ("created_at")
    val createdBy  = uuid    ("created_by") keyOf Users
    val content    = text    ("content")
    val summary    = text    ("summary")
    val isPinned   = bool    ("is_pinned").default(false)

    override val authorColumn = createdBy

    override suspend fun insert(resource: Article): Sr<Article> {
        return Wrap {
            query {
                this.insert {
                    it[uuid] = resource.uuid
                    it[title] = resource.title
                    it[createdAt] = resource.createdAt
                    it[createdBy] = resource.createdBy.uuid
                    it[content] = resource.content
                    it[summary] = resource.summary
                    it[isPinned] = resource.isPinned
                }
            }.get()

            query {
                Categories.batchInsert(resource.categories) {
                    this[Categories.article] = resource.uuid
                    this[Categories.name] = it.name
                }
            }.get()

            return@Wrap resource
        }
    }

    override suspend fun update(resource: Article): Boolean {
        return try {

            query {
                this.update(where = { uuid eq resource.uuid }) {
                    it[uuid] = resource.uuid
                    it[title] = resource.title
                    it[createdAt] = resource.createdAt
                    it[createdBy] = resource.createdBy.uuid
                    it[content] = resource.content
                    it[summary] = resource.summary
                    it[isPinned] = resource.isPinned
                }
            }.get()

            query {
                Categories.deleteWhere { Categories.article eq resource.uuid } == 1
            }.get()

            query {
                Categories.batchInsert(resource.categories) {
                    this[Categories.article] = resource.uuid
                    this[Categories.name] = it.name
                }
            }.get()

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(resource: Article) =
        Wrap {
            super.delete(resource)
            query {
                Categories.deleteWhere { Categories.article eq resource.uuid } == 1
            }

            true
        }

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) =
        SuspendableResult.of<Article, Repository.Exception.Fetching> {
            Article(
                uuid = source[uuid],
                title = source[title],
                createdAt = source[createdAt],
                createdBy = appContext.repository<User>()
                    .get(requestContext, source[createdBy]).get(),
                content = source[content],
                summary = source[summary],
                isPinned = source[isPinned],
                categories = transaction {
                    Categories.select { Categories.article eq source[uuid] }.toList()
                }.map { Categories.convert(it) }
            )
        }

    object Categories : Table() {

        val article = uuid("article").references(Articles.uuid, onDelete = ReferenceOption.CASCADE)
        val name    = varchar("name", 255)

        override val primaryKey = PrimaryKey(
            article,
            name
        )

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) =
            Article.Category(
                name = source[name]
            )

    }

    object Likes: Table("article_likes") {

        val user    = uuid("user").references(Users.uuid, onDelete = ReferenceOption.CASCADE)
        val article = uuid("article").references(Articles.uuid, onDelete = ReferenceOption.CASCADE)

        override val primaryKey = PrimaryKey(
            user,
            article
        )

    }

}
