package blgoify.backend.services.models

import blgoify.backend.database.ResourceTable
import blgoify.backend.resources.models.Resource
import blgoify.backend.util.query

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select

import java.util.*

interface Service<R : Resource> {

    suspend fun getAll(): Set<R>

    suspend fun get(id: UUID): R?

    suspend fun getMatching(table: ResourceTable<R>, predicate: SqlExpressionBuilder.() -> Op<Boolean>): Set<R> = query {
        table.select(predicate).toSet().map { table.convert(it) }.toSet()
    }

    suspend fun add(res: R): Boolean

    suspend fun remove(id: UUID): Boolean

    suspend fun update(res: R): Boolean

}