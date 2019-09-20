package blogify.backend.services

import blogify.backend.database.Users
import blogify.backend.database.Users.uuid
import blogify.backend.resources.User
import blogify.backend.services.handling.deleteWithIdInTable
import blogify.backend.services.handling.fetchWithIdFromTable
import blogify.backend.services.handling.fetchAllFromTable
import blogify.backend.services.models.ResourceResult
import blogify.backend.services.models.ResourceResultSet
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert

import java.util.*

object UserService : Service<User> {

    override suspend fun getAll(): ResourceResultSet<User> = fetchAllFromTable(Users)

    override suspend fun get(id: UUID): ResourceResult<User> = fetchWithIdFromTable(Users, uuid, id)

    override suspend fun add(res: User) = query {
        Users.insert {
            it[uuid]     = res.uuid
            it[username] = res.username
            it[password] = res.password
        }

        return@query res
    }.mapError { e -> Service.Exception.Creating(e) }

    override suspend fun delete(id: UUID) = deleteWithIdInTable(Users, uuid, id)

    override suspend fun update(res: User): ResourceResult<User> {
        TODO("not implemented !")
    }

}