package blogify.backend.database.tables

import blogify.common.util.Sr
import blogify.common.util.Wrap
import blogify.reflect.entity.database.extensions.parentKey
import blogify.reflect.entity.database.extensions.weakKeyFrom
import blogify.reflect.entity.database.handling.query
import blogify.reflect.entity.database.handling.unwrappedQuery
import blogify.reflect.entity.database.QueryContext
import blogify.reflect.entity.database.ResourceTable
import blogify.backend.resources.user.User
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.resources.user.UserSettings

import com.github.kittinunf.result.coroutines.map

import org.jetbrains.exposed.sql.*

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

        bind (uuid, User::uuid)
        bind (username, User::username)
        bind (password, User::password)
        bind (email, User::email)
        bind (name, User::name)
        bind (isAdmin, User::isAdmin)
        bind (biography, User::biography)
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

    override suspend fun update(resource: User): Sr<User> {
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
        }.map { resource }
    }

    override suspend fun convert (
        queryContext: QueryContext,
        source: ResultRow,
        aliasToUse: Alias<ResourceTable<User>>?
    ): Sr<User> = Wrap {
        fun <T> get(column: Column<T>) = if (aliasToUse != null) source[aliasToUse[column]] else source[column]

        User(
            uuid = get(uuid),
            username = get(username),
            password = get(password),
            name = get(name),
            email = get(email),
            isAdmin = get(isAdmin),
            biography = get(biography),
            profilePicture = get(profilePicture)?.let {
                unwrappedQuery {
                    Uploadables.select { Uploadables.fileId eq get(profilePicture)!! }
                        .limit(1).single()
                }.let {
                    Uploadables.convert(queryContext, it).get()
                }
            } ?: StaticFile.None(ContentType.Any),
            coverPicture = get(coverPicture)?.let {
                unwrappedQuery {
                    Uploadables.select { Uploadables.fileId eq get(coverPicture)!! }
                        .limit(1).single()
                }.let {
                    Uploadables.convert(queryContext, it).get()
                }
            } ?: StaticFile.None(ContentType.Any)
        )
    }

}
