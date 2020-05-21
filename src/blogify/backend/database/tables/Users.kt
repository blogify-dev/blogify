package blogify.backend.database.tables

import blogify.backend.database.extensions.parentKey
import blogify.backend.database.extensions.weakKeyFrom
import blogify.backend.database.handling.query
import blogify.backend.database.handling.unwrappedQuery
import blogify.backend.database.models.ResourceTable
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.user.User
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.resources.user.UserSettings
import blogify.backend.util.Sr
import blogify.backend.util.Wrap

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import com.github.kittinunf.result.coroutines.SuspendableResult

import io.ktor.http.ContentType

@Suppress("DuplicatedCode")
object Users : ResourceTable<User>() {

    val username       = text    ("username")
    val password       = text    ("password")
    val email          = text    ("email")
    val name           = text    ("name")
    val settings       = jsonb   ("settings", UserSettings).default(UserSettings())
    val profilePicture = varchar ("profile_picture", 32).nullable() weakKeyFrom Uploadables.fileId
    val coverPicture   = varchar ("cover_picture", 32).nullable() weakKeyFrom Uploadables.fileId
    val isAdmin        = bool    ("is_admin").default(false)
    val biography      = text    ("biography").default("")

    init {
        index(true, username)
    }

    object Follows : Table() {

        val following = parentKey ("following", Users)
        val follower  = parentKey ("follower", Users)

        override val primaryKey = PrimaryKey(following, follower)

    }

    override suspend fun insert(resource: User): Sr<User> {
        return Wrap {
            query {
                insert {
                    it[uuid] = resource.uuid
                    it[username] = resource.username
                    it[password] = resource.password
                    it[email] = resource.email
                    it[name] = resource.name
                    it[profilePicture] =
                        if (resource.profilePicture is StaticFile.Ok) resource.profilePicture.fileId else null
                    it[isAdmin] = resource.isAdmin
                    it[biography] = resource.biography
                }
            }
            return@Wrap resource
        }

    }

    override suspend fun update(resource: User): Boolean {
        return query {
            this.update(where = { uuid eq resource.uuid }) {
                it[uuid] = resource.uuid
                it[username] = resource.username
                it[password] = resource.password
                it[email] = resource.email
                it[name] = resource.name
                it[profilePicture] =
                    if (resource.profilePicture is StaticFile.Ok) resource.profilePicture.fileId else null
                it[coverPicture] =
                    if (resource.coverPicture is StaticFile.Ok) resource.coverPicture.fileId else null
                it[isAdmin] = resource.isAdmin
                it[biography] = resource.biography
            }
        }.get() == 1
    }

    override suspend fun convert(requestContext: RequestContext, source: ResultRow) = Wrap {
        User (
            uuid = source[uuid],
            username = source[username],
            password = source[password],
            name = source[name],
            email = source[email],
            isAdmin = source[isAdmin],
            biography = source[biography],
            profilePicture = source[profilePicture]?.let {
                unwrappedQuery {
                    Uploadables.select { Uploadables.fileId eq source[profilePicture]!! }
                            .limit(1).single()
                }.let {
                    Uploadables.convert(requestContext, it).get()
                }
            } ?: StaticFile.None(ContentType.Any),
            coverPicture = source[coverPicture]?.let {
                unwrappedQuery {
                    Uploadables.select { Uploadables.fileId eq source[coverPicture]!! }
                            .limit(1).single()
                }.let {
                    Uploadables.convert(requestContext, it).get()
                }
            } ?: StaticFile.None(ContentType.Any)
        )
    }

}
