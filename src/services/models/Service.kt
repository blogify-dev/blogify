package me.benjozork.services.models

import me.benjozork.resources.models.Resource

import java.util.*

interface Service<R : Resource> {

    fun getAll(): Set<R>

    fun get(id: UUID): R?

    fun add(res: R): Boolean

    fun remove(id: UUID): Boolean

    fun update(res: R): Boolean

}