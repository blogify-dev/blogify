package blgoify.backend.services

import blgoify.backend.database.Users
import blgoify.backend.database.Users.uuid
import blgoify.backend.resources.User
import blgoify.backend.services.handling.handleResourceDBFetch
import blgoify.backend.services.handling.handleResourceDBFetchAll
import blgoify.backend.services.models.ResourceResult
import blgoify.backend.services.models.ResourceResultSet
import blgoify.backend.services.models.Service
import blgoify.backend.util.booleanReturnQuery

import org.jetbrains.exposed.sql.insert

import java.util.*

object UserService : Service<User> {

    override suspend fun getAll(): ResourceResultSet<User> = handleResourceDBFetchAll(Users)

    override suspend fun get(id: UUID): ResourceResult<User> = handleResourceDBFetch(Users, uuid, id)

    override suspend fun add(res: User) = booleanReturnQuery {
        Users.insert {
            it[uuid]     = res.uuid
            it[name]     = res.name
            it[username] = res.username
            it[password] = res.password
        }
    }

    override suspend fun remove(id: UUID): Boolean {
        return true
    }

    override suspend fun update(res: User): Boolean {
        return true
    }

}