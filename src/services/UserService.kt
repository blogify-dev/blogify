package me.benjozork.services

import me.benjozork.resources.User
import me.benjozork.resources.models.Resource
import me.benjozork.services.models.Service

import java.util.*

class UserService : Service<User> {

    override fun getAll(): Set<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(id: UUID): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(res: User): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(id: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(usr: User): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}