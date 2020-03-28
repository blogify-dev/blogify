package blogify.backend.persistence.postgres.orm.query.internal

import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.FieldSet

import com.andreapivetta.kolor.red

/**
 * Provides internal query building functions and caching.
 *
 * @property table self-explanatory
 *
 * @author Benjozork
 */
class QueryInterface<TResource : Resource>(val table: OrmTable<TResource>) {

    init {
        require(table.fullyMapped()) { "fatal: table is not fully mapped".red() }
    }

    /**
     * Finds the [FieldSet] needed to perform a `select` [Query] that covers the provided [mappings][PropertyMapping]
     *
     * @param mappings the list of mappings that need to be selected by the query. If any value mappings from other classes
     *                 are present, the associative mapping to that class from [klass] **must** be provided with it.
     *
     * @return a [FieldSet] of all the [expressions][org.jetbrains.exposed.sql.Expression] involved, including necessary joins.
     *         the [FieldSet.source] property of the return value will be a [Join].
     */
    fun findFieldsForMappings(mappings: List<PropertyMapping>): FieldSet {
        require (
            mappings.map { it.handle.klass }.let { klasses ->
                klasses.all { it == table.klass || mappings.any { mapping -> mapping is PropertyMapping.AssociativeMapping<*> && mapping.dependency == it } }
            }
        ) { "fatal: mappings from a different class can only be present if an associative mapping to that class is also provided".red() }

        // Create an initial join with the provided associative mappings

        val join = mappings.filterIsInstance<PropertyMapping.AssociativeTableMapping>()
            .fold(Join(table)) { j, m -> m.joinWith(j) }

        // Of all the value mappings in the join, only keep the initially provided ones

        return join.slice(mappings.filterIsInstance<PropertyMapping.ValueMapping>().map { it.column })
    }

}
