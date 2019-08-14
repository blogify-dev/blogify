package me.benjozork.services

import me.benjozork.resources.Article
import me.benjozork.resources.User
import me.benjozork.resources.models.Resource
import me.benjozork.services.models.Service

import java.util.*

class UserService : Service<User> {

    private val userData = mutableSetOf(
        User(name = "Shrek"),
        User(name = "Keanu Reeves")
    ).associateBy { it.uuid }.toMutableMap()

    override fun getAll(): Set<User> {
        return userData.values.toSet()
    }

    override fun get(id: UUID): User? {
        return userData[id]
    }

    override fun add(res: User): Boolean {
        userData[res.uuid] = res
        return true
    }

    override fun remove(id: UUID): Boolean {
        userData.remove(id) ?: return false
        return true
    }

    override fun update(usr: User): Boolean {
        userData[usr.uuid] = usr
        return true    }

}