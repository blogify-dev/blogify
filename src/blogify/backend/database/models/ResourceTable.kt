package blogify.backend.database.models

import blogify.backend.database.binding.SqlBinding
import blogify.backend.database.handling.query
import blogify.backend.database.handling.unwrappedQuery
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.UserCreatedResource
import blogify.backend.resources.reflect.MissingArgumentsException
import blogify.backend.resources.reflect.construct
import blogify.backend.util.*
import blogify.reflect.SlicedProperty
import blogify.reflect.extensions.klass
import blogify.reflect.extensions.okHandle
import blogify.reflect.getPropValueOnInstance

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

import epgx.models.PgTable

import com.github.kittinunf.result.coroutines.map
import com.github.kittinunf.result.coroutines.mapError

import java.util.*

import kotlin.reflect.KProperty1

abstract class ResourceTable<TResource : Resource> : PgTable() {

    val bindings = mutableListOf<SqlBinding<TResource, out Any?, *>>()

    abstract class UserCreated<TResource : UserCreatedResource> : ResourceTable<TResource>() {
        abstract val authorColumn: Column<UUID>
    }

    // Different return types, because generics and nullability doesn't affect JVM signatures, so they can't just be overloads.

    /**
     * Creates a binding between [column] and [property]
     */
    fun <TProperty : Any?> bind(column: Column<TProperty>, property: KProperty1<TResource, TProperty>): SqlBinding.Value<TResource, TProperty> {
        return SqlBinding.Value(this@ResourceTable, column, property)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [column] and [property]
     */
    fun <TProperty : Resource?> bind(column: Column<UUID?>, property: KProperty1<TResource, TProperty>): SqlBinding.NullableReference<TResource, TProperty> {
        return SqlBinding.NullableReference(this@ResourceTable, column, property)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [column] and [property]
     */
    fun <TProperty : Resource> bind(column: Column<UUID>, property: KProperty1<TResource, TProperty>): SqlBinding.Reference<TResource, TProperty> {
        return SqlBinding.Reference(this@ResourceTable, column, property)
            .also { this.bindings += it }
    }

    suspend fun obtainAll(requestContext: RequestContext, limit: Int): SrList<TResource> = Wrap {
        query { this.selectAll().limit(limit).toSet() }.get()
            .map { this.convert(requestContext, it).get() }
    }

    suspend fun obtainListing (
        requestContext: RequestContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TResource>, Boolean>> = Wrap {

        query {
            this.select(selectCondition)
                .orderBy(orderBy, sortOrder)
                //               v-- We add one to check if we reached the end
                .limit(quantity + 1, (page * quantity).toLong())
                .toList()
        }.get().let { results ->
            results.take(quantity).map { this.convert(requestContext, it).get() } to (results.size - 1 == quantity)
        }

    }

    suspend fun obtain(requestContext: RequestContext, id: UUID): Sr<TResource> = Wrap {
        query { this.select { uuid eq id }.single() }.get()
            .let { this.convert(requestContext, it).get() }
    }

    open suspend fun convert(requestContext: RequestContext, source: ResultRow): Sr<TResource> {
        val bindingsData = bindings.map {
            (it.property.okHandle ?: never) to source[it.column]
        }.toMap()

        // hacky ; try to find the class using bindings
        val klass = bindings.firstOrNull()?.property?.klass
            ?: error("at least one column-property binding is necessary to call default impl of convert()")

        return klass.construct(bindingsData, requestContext)
            .mapError { e ->
                if (e is MissingArgumentsException)
                    IllegalStateException("there were missing arguments when calling construct() during SQL conversion - " +
                            "you might want to implement convert() yourself", e)
                else e
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <TResource : Resource, TProperty : Any?> applyBindingToInsertOrUpdate (
        resource: TResource,
        insertStatement: UpdateBuilder<Number>,
        binding: SqlBinding<TResource, TProperty, *>
    ) {
        val slicedProperty = getPropValueOnInstance(resource, binding.property.name, unsafe = true)
        val value = when (slicedProperty) {
            is SlicedProperty.Value -> slicedProperty.value
            is SlicedProperty.NullableValue -> slicedProperty.value
            else -> never
        } as TProperty

        binding.applyToUpdateOrInsert(insertStatement, value)
    }

    open suspend fun insert(resource: TResource): Sr<TResource> {
        return query {
            this.insert {
                for (binding in bindings) {
                    applyBindingToInsertOrUpdate(resource, it, binding)
                }
            }
        }.map { resource }
    }

    open suspend fun update(resource: TResource): Boolean {
        return query {
            this.update({ uuid eq resource.uuid }) {
                for (binding in bindings) {
                    applyBindingToInsertOrUpdate(resource, it, binding)
                }
            }
        }.asBoolean()
    }

    open suspend fun delete(resource: TResource): Boolean = Wrap {
        unwrappedQuery {
            this.deleteWhere { uuid eq resource.uuid }
        }
    }.asBoolean()

    val uuid = uuid("uuid")

    override val primaryKey = PrimaryKey(uuid)

}
