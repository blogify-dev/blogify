package blgoify.backend.services

import blgoify.backend.database.Users
import blgoify.backend.database.Users.convert
import blgoify.backend.resources.User
import blgoify.backend.services.models.Service
import blgoify.backend.util.booleanReturnQuery
import blgoify.backend.util.query

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.util.*

object UserService : Service<User> {

    /*private val userData = mutableSetOf(
        User(name = "Shrek"),
        User(name = "Keanu Reeves")
    ).associateBy { it.uuid }.toMutableMap()*/

    override suspend fun getAll(): Set<User> = query {
        Users.selectAll().toSet().map { convert(it) }
    }.toSet()

    override suspend fun get(id: UUID): User? = query {
        Users.select { Users.uuid eq id }.map { convert(it) }.single()
    }

    override suspend fun add(res: User) = booleanReturnQuery {
        Users.insert {
            it[uuid] = res.uuid;
            it[name] = res.name
        }
    }

    override suspend fun remove(id: UUID): Boolean {
        return true
    }

    override suspend fun update(res: User): Boolean {
        return true
    }

    /*fun getUserByUid(uid: String): User? {
        return userData.values.find { it.firebaseUid == uid }
    }*/

}