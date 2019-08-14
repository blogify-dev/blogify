package me.benjozork.services

import me.benjozork.resources.Article
import me.benjozork.resources.User
import me.benjozork.resources.models.Resource
import me.benjozork.services.models.Service

import java.util.*

object UserService : Service<User> {

    private val userData = mutableSetOf(
        User(name = "Shrek"),
        User(name = "Keanu Reeves")
    ).associateBy { it.uuid }.toMutableMap()

    override suspend fun getAll(): Set<User> {
        return userData.values.toSet()
    }

    override suspend fun get(id: UUID): User? {
        return userData[id]
    }

    override suspend fun add(res: User): Boolean {
        userData[res.uuid] = res
        return true
    }

    override suspend fun remove(id: UUID): Boolean {
        userData.remove(id) ?: return false
        return true
    }

    override suspend fun update(usr: User): Boolean {
        userData[usr.uuid] = usr
        return true
    }

}