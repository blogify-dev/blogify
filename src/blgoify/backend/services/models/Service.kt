package blgoify.backend.services.models

import blgoify.backend.resources.models.Resource

import java.util.*

interface Service<R : Resource> {

    suspend fun getAll(): Set<R>

    suspend fun get(id: UUID): R?

    suspend fun add(res: R): Boolean

    suspend fun remove(id: UUID): Boolean

    suspend fun update(res: R): Boolean

}