package blogify.backend.database.models

import blogify.backend.database.binding.SqlBinding
import blogify.backend.database.extensions.klass
import blogify.backend.database.handling.query
import blogify.backend.database.handling.unwrappedQuery
import blogify.backend.database.optimizer.QueryOptimizer
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

    /**
     * A list of all [bindings][SqlBinding] present for this table
     */
    val bindings = mutableListOf<SqlBinding<TResource, out Any?, *>>()

    abstract class UserCreated<TResource : UserCreatedResource> : ResourceTable<TResource>() {
        abstract val authorColumn: Column<UUID>
    }

    /**
     * Creates a binding between [column] and a [property] containing simple values
     *
     * @param column   the column in which UUIDs of instances of [property] are stored
     * @param property the property of [TResource]`::class` to bind
     */
    fun <TProperty : Any?> bind(column: Column<TProperty>, property: KProperty1<TResource, TProperty>): SqlBinding.Value<TResource, TProperty> {
        return SqlBinding.Value(this@ResourceTable, property, column)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [column] and a [property] containing nullable references to [resources][Resource]
     *
     * @param column   the column in which UUIDs of instances of [property] are stored
     * @param property the property of [TResource]`::class` to bind
     */
    fun <TProperty : Resource?> bind(column: Column<UUID?>, property: KProperty1<TResource, TProperty>): SqlBinding.NullableReference<TResource, TProperty> {
        return SqlBinding.NullableReference(this@ResourceTable, property, column)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [column] and a [property] containing references to [resources][Resource]
     *
     * @param column   the column in which UUIDs of instances of [property] are stored
     * @param property the property of [TResource]`::class` to bind
     */
    fun <TProperty : Resource> bind(column: Column<UUID>, property: KProperty1<TResource, TProperty>): SqlBinding.Reference<TResource, TProperty> {
        return SqlBinding.Reference(this@ResourceTable, property, column)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [table] and a [property]
     *
     * @param table              the table in which instances of [property] are stored for `this` table
     * @param property           the property of [TResource]`::class` to bind
     * @param conversionFunction the function used to convert rows of [table] into instances of [TProperty]
     * @param insertionFunction  the function used to on an [UpdateBuilder] to insert instances of [TProperty] into [table]
     */
    fun <TProperty : Any> bind (
        table: Table,
        property: KProperty1<TResource, Collection<TProperty>>,
        conversionFunction: (ResultRow) -> TProperty,
        insertionFunction: (TResource, TProperty, UpdateBuilder<Number>) -> Unit
    ): SqlBinding.ReferenceToMany<TResource, TProperty> {
        return SqlBinding.ReferenceToMany(this@ResourceTable, property, table, conversionFunction, insertionFunction)
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
    ): Sr<Pair<List<TResource>, Boolean>> = query {
        QueryOptimizer.optimize(this.klass, selectCondition)
            .orderBy(orderBy, sortOrder)
            //               v-- We add one to check if we reached the end
            .limit(quantity + 1, (page * quantity).toLong())
            .toList()
    }.map { results ->
        QueryOptimizer.convertOptimizedRows(requestContext, klass, results)
            .take(quantity) to (results.size - 1 == quantity)
    }

    suspend fun obtain(requestContext: RequestContext, id: UUID): Sr<TResource> = Wrap {
        query { this.select { uuid eq id }.single() }.get()
            .let { this.convert(requestContext, it).get() }
    }

    open suspend fun convert (
        requestContext: RequestContext,
        source: ResultRow,
        aliasToUse: Alias<ResourceTable<TResource>>? = null
    ): Sr<TResource> {
        fun <T> get(column: Column<T>) = if (aliasToUse != null) source[aliasToUse[column]] else source[column]

        val bindingsData = bindings.map {
            when (val binding = it) {
                is SqlBinding.HasColumn<*> -> {
                    (binding.property.okHandle ?: never) to get(binding.column)
                }
                is SqlBinding.ReferenceToMany<*, *> -> {
                    val resourceUuid = get(this.uuid)

                    (binding.property.okHandle ?: never) to unwrappedQuery {
                        binding.otherTable.select { binding.otherTableFkToPkCol eq resourceUuid }
                            .toSet().map { row -> binding.conversionFunction(row) }
                    }
                }
                else -> never
            }

        }.toMap()

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
        assert(binding !is SqlBinding.ReferenceToMany<*, *>) { "applyBindingToInsertOrUpdate should not be called on SqlBinding.ReferenceToMany" }

        val slicedProperty = getPropValueOnInstance(resource, binding.property.name, unsafe = true)
        val value = when (slicedProperty) {
            is SlicedProperty.Value -> slicedProperty.value
            is SlicedProperty.NullableValue -> slicedProperty.value
            else -> never
        } as TProperty

        binding.applyToUpdateOrInsert(insertStatement, value)
    }

    open suspend fun insert(resource: TResource): Sr<TResource> = Wrap {
        unwrappedQuery {
            this.insert {
                for (binding in bindings) {
                    if (binding !is SqlBinding.ReferenceToMany<*, *>)
                        applyBindingToInsertOrUpdate(resource, it, binding)
                }
            }
        }

        for (binding in bindings.filterIsInstance<SqlBinding.ReferenceToMany<TResource, Any>>()) {
            val instances = binding.property.get(resource)
            val bindingTable = binding.otherTable

            query {
                bindingTable.batchInsert(instances) { item ->
                    binding.insertionFunction(resource, item, this)
                }
            }.mapError { IllegalStateException("error occurred during update in ReferenceToMany table", it) }
                .get()
        }
    }.map { resource }

    open suspend fun update(resource: TResource): Boolean = Wrap {
        unwrappedQuery {
            this.update({ uuid eq resource.uuid }) {
                for (binding in bindings) {
                    if (binding !is SqlBinding.ReferenceToMany<*, *>)
                        applyBindingToInsertOrUpdate(resource, it, binding)
                }
            }
        }

        for (binding in bindings.filterIsInstance<SqlBinding.ReferenceToMany<TResource, Any>>()) {
            val newInstances = binding.property.get(resource)

            query {
                binding.otherTable.deleteWhere { binding.otherTableFkToPkCol eq resource.uuid }
            }.mapError { IllegalStateException("error occurred during update in ReferenceToMany table", it) }
                .get()

            query {
                binding.otherTable.batchInsert(newInstances) { item -> binding.insertionFunction(resource, item, this) }
            }.mapError { IllegalStateException("error occurred during update in ReferenceToMany table", it) }
                .get()
        }
    }.asBoolean()

    open suspend fun delete(resource: TResource): Boolean = Wrap {
        unwrappedQuery {
            this.deleteWhere { uuid eq resource.uuid }
        }
    }.asBoolean()

    val uuid = uuid("uuid")

    override val primaryKey = PrimaryKey(uuid)

}
