package blogify.database

import blogify.common.util.*
import blogify.reflect.entity.Entity
import blogify.reflect.entity.instantiation.MissingArgumentsException
import blogify.reflect.SlicedProperty
import blogify.reflect.extensions.handle
import blogify.reflect.extensions.okHandle
import blogify.reflect.getPropValueOnInstance
import blogify.database.handling.query
import blogify.database.handling.unwrappedQuery
import blogify.database.binding.SqlBinding
import blogify.database.extensions.klass
import blogify.database.optimizer.QueryOptimizer

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

import epgx.models.PgTable

import com.github.kittinunf.result.coroutines.map
import com.github.kittinunf.result.coroutines.mapError

import java.util.*

import kotlin.reflect.KProperty1

/**
 * Generic table for storing [entities][Entity] inside a postgres table.
 *
 * Using [SQL bindings][SqlBinding], this takes care of automagically running CRUD operations to and from the database.
 *
 * For example, if you provide all necessary bindings for a class, simply calling [EntityTable.insert] should be
 * enough to store an instance of [TEntity] in the table. Likewise for [delete], [update], [obtain], [obtainListing]
 * and [obtainAll].
 *
 * @param TEntity the type of [Entity] to be stored
 *
 * @see PgTable
 * @see Entity
 * @see SqlBinding
 *
 * @author Benjozork, hamza1311
 */
abstract class EntityTable<TEntity : Entity> : PgTable() {

    /**
     * A list of all [bindings][SqlBinding] present for this table
     */
    val bindings = mutableListOf<SqlBinding<TEntity, out Any?, *>>()

    /**
     * Creates a binding between [column] and a [property] containing simple values
     *
     * @param column   the column in which UUIDs of instances of [property] are stored
     * @param property the property of [TEntity]`::class` to bind
     */
    fun <TProperty : Any?> bind(column: Column<TProperty>, property: KProperty1<TEntity, TProperty>): SqlBinding.Value<TEntity, TProperty> {
        return SqlBinding.Value(this@EntityTable, property, column)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [column] and a [property] containing nullable references to [entity][Entity]
     *
     * @param column   the column in which UUIDs of instances of [property] are stored
     * @param property the property of [TEntity]`::class` to bind
     */
    fun <TProperty : Entity?> bind(column: Column<UUID?>, property: KProperty1<TEntity, TProperty>): SqlBinding.NullableReference<TEntity, TProperty> {
        return SqlBinding.NullableReference(this@EntityTable, property, column)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [column] and a [property] containing references to [entity][Entity]
     *
     * @param column   the column in which UUIDs of instances of [property] are stored
     * @param property the property of [TEntity]`::class` to bind
     */
    fun <TProperty : Entity> bind(column: Column<UUID>, property: KProperty1<TEntity, TProperty>): SqlBinding.Reference<TEntity, TProperty> {
        return SqlBinding.Reference(this@EntityTable, property, column)
            .also { this.bindings += it }
    }

    /**
     * Creates a binding between [table] and a [property]
     *
     * @param table              the table in which instances of [property] are stored for `this` table
     * @param property           the property of [TEntity]`::class` to bind
     * @param conversionFunction the function used to convert rows of [table] into instances of [TProperty]
     * @param insertionFunction  the function used to on an [UpdateBuilder] to insert instances of [TProperty] into [table]
     */
    fun <TProperty : Any> bind (
        table: Table,
        property: KProperty1<TEntity, Collection<TProperty>>,
        conversionFunction: (ResultRow) -> TProperty,
        insertionFunction: (TEntity, TProperty, UpdateBuilder<Number>) -> Unit
    ): SqlBinding.ReferenceToMany<TEntity, TProperty> {
        return SqlBinding.ReferenceToMany(this@EntityTable, property, table, conversionFunction, insertionFunction)
            .also { this.bindings += it }
    }

    /**
     * Returns an arbitrary list of [limit] items from the table
     */
    @Deprecated(message = "please use obtainListing() instead")
    suspend fun obtainAll(queryContext: QueryContext, limit: Int): SrList<TEntity> =
        Wrap {
            query { this.selectAll().limit(limit).toSet() }.get()
                .map { this.convert(queryContext, it).get() }
        }

    /**
     * Queries the table using a listing specification
     *
     * @param queryContext  [QueryContext] for caching
     * @param selectCondition the `where` condition to apply in the query
     * @param quantity        the quantity of items to fetch
     * @param page            the page number
     * @param orderBy         which column to specify in the `order by` clause
     * @param sortOrder       the sort order [SortOrder] (`order by desc / asc`)
     */
    suspend fun obtainListing (
        queryContext: QueryContext,
        selectCondition: SqlExpressionBuilder.() -> Op<Boolean>,
        quantity: Int,
        page: Int,
        orderBy: Column<*>,
        sortOrder: SortOrder = SortOrder.ASC
    ): Sr<Pair<List<TEntity>, Boolean>> = query {
        QueryOptimizer.optimize(this.klass, selectCondition)
            .orderBy(orderBy, sortOrder)
            //               v-- We add one to check if we reached the end
            .limit(quantity + 1, (page * quantity).toLong())
            .toList()
    }.map { results ->
        QueryOptimizer.convertOptimizedRows(queryContext, klass, results)
            .take(quantity) to (results.size - 1 == quantity)
    }

    /**
     * Returns a specific entity with a certain UUID from the table
     *
     * @param queryContext [QueryContext] for caching
     * @param id             the [UUID] of the entity
     */
    suspend fun obtain(queryContext: QueryContext, id: UUID): Sr<TEntity> =
        Wrap {
            query { this.select { uuid eq id }.single() }.get()
                .let { this.convert(queryContext, it).get() }
        }

    /**
     * Performs the conversion between a single [ResultRow] and an instance of [TEntity]. Should not be used directly.
     *
     * @param queryContext  [QueryContext] for caching
     * @param source          the result row to get the data from
     * @param aliasToUse      a [table alias][Alias] to specify which columns should be used when data for multiple
     *                        instances of [TEntity] might be present in a single row
     */
    open suspend fun convert (
        queryContext: QueryContext,
        source: ResultRow,
        aliasToUse: Alias<EntityTable<TEntity>>? = null
    ): Sr<TEntity> {
        fun <T> get(column: Column<T>) = if (aliasToUse != null) source[aliasToUse[column]] else source[column]

        val bindingsData = bindings.map {
            when (val binding = it) {
                is SqlBinding.HasColumn<*> -> {
                    (binding.property.okHandle ?: never) to get(binding.column)
                }
                is SqlBinding.ReferenceToMany<*, *> -> {
                    val entityId = get(this.uuid)

                    (binding.property.okHandle ?: never) to unwrappedQuery {
                        binding.otherTable.select { binding.otherTableFkToPkCol eq entityId }
                            .toSet().map { row -> binding.conversionFunction(row) }
                    }
                }
                else -> never
            }

        }.toMap()

        return with(queryContext) {
            klass.construct(bindingsData)
                .mapError { e ->
                    if (e is MissingArgumentsException)
                        IllegalStateException("there were missing arguments when calling construct() during SQL conversion - " +
                                "you might want to implement convert() yourself", e)
                    else e
                }
        }
    }

    /**
     * Applies a value of the binding's property for [entity] on an [`insert` / `update` statement][insertStatement]
     *
     * @param entity          the instance on which the value of [binding]'s property should be collected
     * @param insertStatement an [UpdateBuilder] on which to set the binding's column to the value
     * @param binding         the [SqlBinding] we are working with
     */
    @Suppress("UNCHECKED_CAST")
    private fun <TEntity : Entity, TProperty : Any?> applyBindingToInsertOrUpdate (
        entity: TEntity,
        insertStatement: UpdateBuilder<Number>,
        binding: SqlBinding<TEntity, TProperty, *>
    ) {
        assert(binding !is SqlBinding.ReferenceToMany<*, *>) { "applyBindingToInsertOrUpdate should not be called on SqlBinding.ReferenceToMany" }

        val slicedProperty = getPropValueOnInstance (
            instance = entity,
            propertyHandle = binding.property.handle,
            unsafe = true
        )

        val value = when (slicedProperty) {
            is SlicedProperty.Value -> slicedProperty.value
            is SlicedProperty.NullableValue -> slicedProperty.value
            else -> never
        } as TProperty

        binding.applyToUpdateOrInsert(insertStatement, value)
    }

    /**
     * Inserts [entity] into the table
     *
     * @return the entity itself if the insert was successful
     */
    open suspend fun insert(entity: TEntity): Sr<TEntity> = query {
        this.insert {
            for (binding in bindings) {
                if (binding !is SqlBinding.ReferenceToMany<*, *>)
                    applyBindingToInsertOrUpdate(entity, it, binding)
            }
        }

        for (binding in bindings.filterIsInstance<SqlBinding.ReferenceToMany<TEntity, Any>>()) {
            val instances = binding.property.get(entity)
            val bindingTable = binding.otherTable

            bindingTable.batchInsert(instances) { item ->
                binding.insertionFunction(entity, item, this)
            }
        }
    }.map { entity }

    /**
     * Updates [entity] into the table using it's uuid for finding the old version
     *
     * @return whether or not the update was successful
     */
    open suspend fun update(entity: TEntity): Sr<TEntity> = query {
        this.update({ uuid eq entity.uuid }) {
            for (binding in bindings) {
                if (binding !is SqlBinding.ReferenceToMany<*, *>)
                    applyBindingToInsertOrUpdate(entity, it, binding)
            }
        }

        for (binding in bindings.filterIsInstance<SqlBinding.ReferenceToMany<TEntity, Any>>()) {
            val newInstances = binding.property.get(entity)

            binding.otherTable.deleteWhere { binding.otherTableFkToPkCol eq entity.uuid }

            binding.otherTable.batchInsert(newInstances) { item -> binding.insertionFunction(entity, item, this) }
        }
    }.map { entity }

    /**
     * Deletes [entity] from table using it's uuid for finding the item to delete
     *
     * @return whether or not the deletion was successful
     */
    open suspend fun delete(entity: TEntity): Boolean = query {
        unwrappedQuery {
            this.deleteWhere { uuid eq entity.uuid }
        }
    }.assertGet().let { true }

    val uuid = uuid("uuid")

    override val primaryKey = PrimaryKey(uuid)

}
