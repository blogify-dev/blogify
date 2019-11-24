package blogify.backend.services

import blogify.backend.database.Users
import blogify.backend.resources.User
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query
import blogify.backend.resources.static.models.StaticResourceHandle

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

object UserService : Service<User>(Users) {

    override suspend fun add(res: User) = query {
        Users.insert {
            it[uuid]           = res.uuid
            it[username]       = res.username
            it[password]       = res.password
            it[name]           = res.name
            it[email]          = res.email
            it[isAdmin] = res.isAdmin
        }

        return@query res
    }.mapError { e -> Exception.Creating(e) }

    override suspend fun update(res: User) = query {
        Users.update(where = { Users.uuid eq res.uuid }) {
            it[username]       = res.username
            it[name]           = res.name
            it[email]          = res.email
            it[profilePicture] = (res.profilePicture as? StaticResourceHandle.Ok)?.fileId
            it[isAdmin] = res.isAdmin
        }

        return@query res
    }.mapError { e -> Exception.Updating(e) }


}
