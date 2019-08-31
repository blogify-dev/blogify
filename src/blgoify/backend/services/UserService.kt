package blgoify.backend.services

import blgoify.backend.database.Users
import blgoify.backend.database.Users.uuid
import blgoify.backend.resources.User
import blgoify.backend.services.handling.handleResourceDBDelete
import blgoify.backend.services.handling.handleResourceDBFetch
import blgoify.backend.services.handling.handleResourceDBFetchAll
import blgoify.backend.services.models.ResourceResult
import blgoify.backend.services.models.ResourceResultSet
import blgoify.backend.services.models.Service
import blgoify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert

import java.util.*

object UserService : Service<User> {

    override suspend fun getAll(): ResourceResultSet<User> = handleResourceDBFetchAll(Users)

    override suspend fun get(id: UUID): ResourceResult<User> = handleResourceDBFetch(Users, uuid, id)

    override suspend fun add(res: User) = query {
        Users.insert {
            it[uuid]     = res.uuid
            it[name]     = res.name
            it[username] = res.username
            it[password] = res.password
        }

        return@query res
    }.mapError { e -> Service.Exception.Creating(e) }

    override suspend fun delete(id: UUID) = handleResourceDBDelete(Users, uuid, id)

    override suspend fun update(res: User): ResourceResult<User> {
        TODO("not implemented !")
    }

}