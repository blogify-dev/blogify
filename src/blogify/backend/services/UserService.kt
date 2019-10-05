package blogify.backend.services

import blogify.backend.database.Users
import blogify.backend.resources.User
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert

object UserService : Service<User>(Users) {

    override suspend fun add(res: User) = query {
        Users.insert {
            it[uuid]     = res.uuid
            it[username] = res.username
            it[password] = res.password
            it[name] = res.name
            it[email] = res.email
        }

        return@query res
    }.mapError { e -> Service.Exception.Creating(e) }

    override suspend fun update(res: User): ResourceResult<User> {
        TODO("not implemented !")
    }

}
