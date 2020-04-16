@file:Suppress("RemoveRedundantQualifierName", "DuplicatedCode")

package blogify.backend.database

import blogify.backend.appContext
import blogify.backend.database.handling.query
import blogify.backend.events.models.Event
import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.image.ImageMetadata
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.sanitize
import blogify.backend.util.*

import io.ktor.http.ContentType

import org.jetbrains.exposed.sql.ReferenceOption.*
import org.jetbrains.exposed.sql.transactions.transaction

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.getOrElse
import com.github.kittinunf.result.coroutines.map
import java.time.Instant

import java.util.UUID

import com.andreapivetta.kolor.red
import org.jetbrains.exposed.sql.*

abstract class ResourceTable< R: Resource> : Table() {

    open val authorColumn: Column<UUID>? = null

    suspend fun obtainAll(requestContext: RequestContext, limit: Int): SrList<R> = Wrap {
        query { this.selectAll().limit(limit).toSet() }.get()
            .map { this.convert(requestContext, it).get() }
    }

    suspend fun obtainListing(
        requestContext: RequestContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<R>, Boolean>> = Wrap {

        if (authorColumn == null)
            error("fatal: tried to query listing but author column was not set on table".red())
        println("orderBy: $orderBy")
        println("sortOrder: $sortOrder")
        query {
            this.select(selectCondition) //    v-- We add one to check if we reached the end
                .orderBy(orderBy, sortOrder)
                .limit(quantity + 1, (page * quantity).toLong())
                .toList()
        }.get().let { results ->
            results.take(quantity).map { this.convert(requestContext, it).get() } to (results.size - 1 == quantity)
        }
    }

    suspend fun obtain(requestContext: RequestContext, id: UUID): Sr<R> = Wrap {
        query { this.select { uuid eq id }.single() }.get()
            .let { this.convert(requestContext, it).get() }
    }

    abstract suspend fun convert(requestContext: RequestContext, source: ResultRow): Sr<R>

    abstract suspend fun insert(resource: R): Sr<R>

    abstract suspend fun update(resource: R): Boolean

    open suspend fun delete(resource: R): Sr<Boolean> = Wrap {
        query {
            this.deleteWhere { uuid eq resource.uuid }
        }

        true
    }

    val uuid = uuid("uuid")

    override val primaryKey = PrimaryKey(uuid)

}

object Articles : ResourceTable<Article>() {

    val title      = varchar ("title", 512)
    val createdAt  = integer ("created_at")
    val createdBy  = uuid    ("created_by").references(Users.uuid, onDelete = SET_NULL)
    val content    = text    ("content")
    val summary    = text    ("summary")
    val isPinned   = bool    ("is_pinned").default(false)

    override val authorColumn = createdBy

    override suspend fun insert(resource: Article): Sr<Article> {
        return Wrap {
            query {
                this.insert {
                    it[uuid]      = resource.uuid
                    it[title]     = resource.title
                    it[createdAt] = resource.createdAt
                    it[createdBy] = resource.createdBy.uuid
                    it[content]   = resource.content
                    it[summary]   = resource.summary
                    it[isPinned]  = resource.isPinned
                }
            }.get()

            query {
                Categories.batchInsert(resource.categories) {
                    this[Categories.article] = resource.uuid
                    this[Categories.name]    = it.name
                }
            }.get()

            return@Wrap resource
        }
    }

    override suspend fun update(resource: Article): Boolean {
        return try {

            query {
                this.update(where = { uuid eq resource.uuid }) {
                    it[uuid]      = resource.uuid
                    it[title]     = resource.title
                    it[createdAt] = resource.createdAt
                    it[createdBy] = resource.createdBy.uuid
                    it[content]   = resource.content
                    it[summary]   = resource.summary
                    it[isPinned]  = resource.isPinned
                }
            }.get()

            query {
                Categories.deleteWhere { Categories.article eq resource.uuid } == 1
            }.get()

            query {
                Categories.batchInsert(resource.categories) {
                    this[Categories.article] = resource.uuid
                    this[Categories.name]    = it.name
                }
            }.get()

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(resource: Article) = Wrap {
        super.delete(resource)
        query {
            Categories.deleteWhere { Categories.article eq resource.uuid } == 1
        }

        true
    }

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) = Sr.of<Article, Repository.Exception.Fetching> {
        Article (
            uuid       = source[uuid],
            title      = source[title],
            createdAt  = source[createdAt],
            createdBy  = appContext.repository<User>().get(requestContext, source[createdBy]).get(),
            content    = source[content],
            summary    = source[summary],
            isPinned   = source[isPinned],
            categories = transaction {
                Categories.select { Categories.article eq source[uuid] }.toList()
            }.map { Categories.convert(it) }
        )
    }

    object Categories : Table() {

        val article = uuid("article").references(Articles.uuid, onDelete = CASCADE)
        val name    = varchar("name", 255)

        override val primaryKey = PrimaryKey(article, name)

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) = Article.Category (
            name = source[name]
        )

    }

    object Likes: Table("article_likes") {

        val user    = uuid("user").references(Users.uuid, onDelete = CASCADE)
        val article = uuid("article").references(Articles.uuid, onDelete = CASCADE)

        override val primaryKey = PrimaryKey(user, article)

    }

}

@Suppress("DuplicatedCode")
object Users : ResourceTable<User>() {

    val username       = varchar ("username", 255)
    val password       = varchar ("password", 255)
    val email          = varchar ("email", 255)
    val name           = varchar ("name", 255)
    val profilePicture = varchar ("profile_picture", 32).references(Uploadables.fileId, onDelete = SET_NULL, onUpdate = RESTRICT).nullable()
    val coverPicture   = varchar ("cover_picture", 32).references(Uploadables.fileId, onDelete = SET_NULL, onUpdate = RESTRICT).nullable()
    val isAdmin        = bool    ("is_admin")

    init {
        index(true, username)
    }

    object Follows : Table() {

        val following = uuid("following").references(Users.uuid, onDelete = CASCADE)
        val follower = uuid("follower").references(Users.uuid, onDelete = CASCADE)

        override val primaryKey = PrimaryKey(following, follower)

    }

    override suspend fun insert(resource: User): Sr<User> {
        return Wrap {
            query {
                Users.insert {
                    it[uuid]           = resource.uuid
                    it[username]       = resource.username
                    it[password]       = resource.password
                    it[email]          = resource.email
                    it[name]           = resource.name
                    it[profilePicture] = if (resource.profilePicture is StaticResourceHandle.Ok) resource.profilePicture.fileId else null
                    it[isAdmin]        = resource.isAdmin
                }
            }
            return@Wrap resource
        }

    }

    override suspend fun update(resource: User): Boolean {
        return query {
            this.update(where = { uuid eq resource.uuid }) {
                it[uuid]           = resource.uuid
                it[username]       = resource.username
                it[password]       = resource.password
                it[email]          = resource.email
                it[name]           = resource.name
                it[profilePicture] = if (resource.profilePicture is StaticResourceHandle.Ok) resource.profilePicture.fileId else null
                it[coverPicture]   = if (resource.coverPicture is StaticResourceHandle.Ok) resource.coverPicture.fileId else null
                it[isAdmin]        = resource.isAdmin
            }
        }.get() == 1
    }

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) = SuspendableResult.of<User, Repository.Exception.Fetching> {
        User (
            uuid           = source[uuid],
            username       = source[username],
            password       = source[password],
            name           = source[name],
            email          = source[email],
            isAdmin        = source[isAdmin],
            profilePicture = source[profilePicture]?.let { transaction {
                Uploadables.select { Uploadables.fileId eq source[profilePicture]!! }.limit(1).single()
            }.let { Uploadables.convert(requestContext, it).get() } } ?: StaticResourceHandle.None(ContentType.Any),
            coverPicture = source[coverPicture]?.let { transaction {
                Uploadables.select { Uploadables.fileId eq source[coverPicture]!! }.limit(1).single()
            }.let { Uploadables.convert(requestContext, it).get() } } ?: StaticResourceHandle.None(ContentType.Any)
        )
    }

}

object Comments : ResourceTable<Comment>() {

    val commenter     = uuid ("commenter").references(Users.uuid, onDelete = SET_NULL)
    val article       = uuid ("article").references(Articles.uuid, onDelete = CASCADE)
    val content       = text ("content")
    val parentComment = uuid ("parent_comment").references(uuid, onDelete = CASCADE).nullable()
    val createdAt     = integer("created_at")

    override val authorColumn = commenter

    object Likes: Table("comment_likes") {

        val user    = Comments.Likes.uuid("user").references(Users.uuid, onDelete = CASCADE)
        val comment = Comments.Likes.uuid("comment").references(Comments.uuid, onDelete = CASCADE)

        override val primaryKey = PrimaryKey(user, comment)
    }

    override suspend fun insert(resource: Comment): Sr<Comment> {
        return Wrap {
            query {
                this.insert {
                    it[uuid]          = resource.uuid
                    it[commenter]     = resource.commenter.uuid
                    it[article]       = resource.article.uuid
                    it[content]       = resource.content
                    it[parentComment] = resource.parentComment?.uuid
                    it[createdAt]     = resource.createdAt
                }
            }
            return@Wrap resource
        }
    }

    override suspend fun update(resource: Comment): Boolean {
        return query {
            this.update(where = { Users.uuid eq resource.uuid }) {
                it[uuid]          = resource.uuid
                it[commenter]     = resource.commenter.uuid
                it[article]       = resource.article.uuid
                it[content]       = resource.content
                it[parentComment] = resource.parentComment?.uuid
            }
        }.get() == 1
    }

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) = SuspendableResult.of<Comment, Repository.Exception.Fetching> {
        Comment (
            uuid          = source[uuid],
            content       = source[content],
            article       = appContext.repository<Article>().get(requestContext, source[article]).get(),
            commenter     = appContext.repository<User>().get(requestContext, source[commenter]).get(),
            parentComment = source[parentComment]?.let { appContext.repository<Comment>().get(requestContext, it).get() },
            createdAt     = source[createdAt]
        )
    }

}

object Uploadables : Table() {

    val fileId      = varchar ("id", 32)
    val contentType = varchar ("content_type", 64)

    override val primaryKey = PrimaryKey(fileId)

    suspend fun convert(@Suppress("UNUSED_PARAMETER") requestContext: RequestContext, source: ResultRow) = Wrap {

        // Parse the content type
        val contentType = ContentType.parse(source[contentType])

        return@Wrap if (contentType matches ContentType.Image.Any) { // If it's an image, read metadata
            StaticResourceHandle.Ok.Image (
                contentType = contentType,
                fileId  = source[fileId],
                metadata = ImageUploadablesMetadata.obtain(requestContext, source[fileId])
                    .getOrElse(ImageMetadata(0, 0))
            )
        } else { // If not, just return a normal handle
            StaticResourceHandle.Ok (
                contentType = contentType,
                fileId  = source[fileId]
            )
        }
    }

}

object ImageUploadablesMetadata : Table("image_metadata") {

    val handleId = varchar ("id", 32).references(Uploadables.fileId, onDelete = CASCADE, onUpdate = RESTRICT)
    val width    = integer ("width").default(0)
    val height   = integer ("height").default(0)

    suspend fun obtain(requestContext: RequestContext, id: String): Sr<ImageMetadata> = Wrap {
        query { this.select { handleId eq id }.single() }.get()
            .let { this.convert(requestContext, it).get() }
    }

    private suspend fun convert(@Suppress("UNUSED_PARAMETER") requestContext: RequestContext, source: ResultRow) = Wrap {
        ImageMetadata (
            source[width],
            source[height]
        )
    }

}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
object Notifications : Table("notifications") {

    val klass     = text    ("class")
    val timestamp = integer ("timestamp")
    val emitter   = uuid    ("emitter").references(Users.uuid, onDelete = CASCADE, onUpdate = CASCADE)
    val data      = jsonb<Map<String, Any?>>("data")

    suspend fun convert(request: RequestContext, source: ResultRow): Map<String, Any?> {
         return mutableMapOf (
             "data"      to source[data],
             "emitter"   to source[emitter],
             "timestamp" to Instant.ofEpochSecond(source[timestamp].toLong()),
             "klass"     to source[klass]
         ).toMap()
    }

    suspend fun insert(event: Event): Sr<Event> {
        return Wrap {
            query {
                insert {
                    it[data]      = event.sanitize()
                    it[emitter]   = event.emitter.uuid
                    it[timestamp] = event.timestamp.epochSecond.toInt()
                    it[klass]     = event::class.qualifiedName ?: never
                }
            }
        }.map { event }
    }

}
