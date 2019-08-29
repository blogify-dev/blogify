package blgoify.backend.services

import blgoify.backend.database.Users
import blgoify.backend.database.Users.convert
import blgoify.backend.resources.User
import blgoify.backend.services.models.Service
import blgoify.backend.util.booleanReturnQuery
import blgoify.backend.util.query

import com.github.kittinunf.result.coroutines.SuspendableResult

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.util.*

object UserService : Service<User> {

    override suspend fun getAll(): Set<User> = query {
        Users.selectAll().toSet()
    }.map { convert(it).get() }.toSet()

    override suspend fun get(id: UUID): SuspendableResult<User, Service.Exception.Fetching> = query {
        Users.select { Users.uuid eq id }.singleOrNull()
    }?.let { convert(it) } ?: error("")

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