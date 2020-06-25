package blogify.backend.database.tables

import blogify.common.util.Sr
import blogify.common.util.Wrap
import blogify.database.extensions.parentKey
import blogify.database.extensions.weakKeyFrom
import blogify.database.handling.query
import blogify.database.handling.unwrappedQuery
import blogify.database.QueryContext
import blogify.database.EntityTable
import blogify.backend.resources.user.User
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.resources.user.UserSettings

import com.github.kittinunf.result.coroutines.map

import org.jetbrains.exposed.sql.*

import io.ktor.http.ContentType

@Suppress("DuplicatedCode")
@ExperimentalStdlibApi
object Users : EntityTable<User>() {

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

    override suspend fun insert(entity: User): Sr<User> {
        return Wrap {
            query {
                insert {
                    it[uuid] = entity.uuid
                    it[username] = entity.username
                    it[password] = entity.password
                    it[email] = entity.email
                    it[name] = entity.name
                    it[profilePicture] =
                        if (entity.profilePicture is StaticFile.Ok) entity.profilePicture.fileId else null
                    it[isAdmin] = entity.isAdmin
                    it[biography] = entity.biography
                }
            }
            return@Wrap entity
        }

    }

    override suspend fun update(entity: User): Sr<User> {
        return query {
            this.update(where = { uuid eq entity.uuid }) {
                it[uuid] = entity.uuid
                it[username] = entity.username
                it[password] = entity.password
                it[email] = entity.email
                it[name] = entity.name
                it[profilePicture] =
                    if (entity.profilePicture is StaticFile.Ok) entity.profilePicture.fileId else null
                it[coverPicture] =
                    if (entity.coverPicture is StaticFile.Ok) entity.coverPicture.fileId else null
                it[isAdmin] = entity.isAdmin
                it[biography] = entity.biography
            }
        }.map { entity }
    }

    override suspend fun convert (
        queryContext: QueryContext,
        source: ResultRow,
        aliasToUse: Alias<EntityTable<User>>?
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
