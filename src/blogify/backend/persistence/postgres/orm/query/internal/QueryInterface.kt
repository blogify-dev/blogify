package blogify.backend.persistence.postgres.orm.query.internal

import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.Column
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
     * @param mappings         the list of mappings that need to be selected by the query. If any value mappings from other classes
     *                         are present, the associative mapping to that class from [klass] **must** be provided with it.
     *
     * @param pruneImplicit    whether or not fields implicitly added by any joins should be removed to only keep explicitly specified ones.
     *                         Defaults to `true`. Can bee changed to `false` to get all mappings in a joined table.
     *
     * @param keepImplicitKeys if [pruneImplicit] is set to `true`, this parameter will keep FKs of [table] point to the PK of a joined table
     *
     * @return a [FieldSet] of all the [expressions][org.jetbrains.exposed.sql.Expression] involved, including necessary joins.
     *         the [FieldSet.source] property of the return value will be a [Join].
     */
    fun findFieldsForMappings(mappings: List<PropertyMapping>, pruneImplicit: Boolean = true, keepImplicitKeys: Boolean = false): FieldSet {
        require (
            mappings.map { it.handle.klass }.let { klasses ->
                klasses.all { it == table.klass || mappings.any { mapping -> mapping is PropertyMapping.AssociativeMapping<*> && mapping.dependency == it } }
            }
        ) { "fatal: mappings from a different class can only be present if an associative mapping to that class is also provided".red() }

        // Create an initial join with the provided associative mappings

        val join = mappings.filterIsInstance<PropertyMapping.AssociativeTableMapping>()
            .fold(Join(table)) { j, m -> m.joinWith(j) }

        // Of all the value mappings in the join, only keep the initially provided ones

        return if (pruneImplicit) {
            if (keepImplicitKeys) {
                join.slice(join.fields.filter {
                    if (it !is Column<*>) return@filter false

                    val isFkFromTableToPkOfJoinedTable = it.foreignKey != null && it.foreignKey!!.from.table == this.table && it.foreignKey!!.target.indexInPK != null
                    val isInExplicitMappings           = it in mappings.filterIsInstance<PropertyMapping.ValueMapping>().map { m -> m.column }

                    isFkFromTableToPkOfJoinedTable || isInExplicitMappings
                })
            } else join.slice(mappings.filterIsInstance<PropertyMapping.ValueMapping>().map { it.column })
        } else join
    }

}
