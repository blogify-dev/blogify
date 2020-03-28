package blogify.backend.persistence.postgres.orm.models

import blogify.backend.persistence.postgres.orm.query.internal.QueryInterface
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.okHandle

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

import java.util.UUID

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

import com.andreapivetta.kolor.red

/**
 * Table generated by the ORM
 *
 * @property klass    the [KClass] the table was generated for
 * @property mappings the [mappings][PropertyMapping] to use for building the table
 *
 * @author Benjozork
 */
class OrmTable<TResource : Resource> (
    val klass: KClass<TResource>,
    val mappings: Set<PropertyMapping>
) : SimpleOrmTable(name = klass.simpleName ?: error("klass must not be an anonymous object literal")) {

    override var primaryKey: PrimaryKey? = null

    val dependencyTables = mutableSetOf<Table>()

    inline operator fun <reified T : TResource> get(property: KProperty1<T, *>) =
        mappings.first { it.handle == property.okHandle() }

    inline operator fun <reified T : TResource> get(handle: PropMap.PropertyHandle.Ok<T>) =
        mappings.first { it.handle == handle }

    val queryInterface by lazy { QueryInterface(this) }

    /**
     * Contains the columns that serves as the identifier (a UUID) for the entity stored in this table. This should always be the only
     * mapping of type [PropertyMapping.IdentifierMapping].
     */
    @Suppress("UNCHECKED_CAST")
    val identifyingColumn: Column<UUID>
        get() = mappings.filterIsInstance<PropertyMapping.IdentifierMapping>().firstOrNull()?.column
            ?: error("fatal: none or more than one identifier mappings in '${klass.simpleName}'".red())

    /**
     * Check the mapping status of the table
     *
     * @return whether or not the table is fully mapped (in other words, whether or not there are still [PropertyMapping.AssociativeMapping] mappings
     *         to complete)
     */
    fun fullyMapped() = this.mappings
        .filterIsInstance<PropertyMapping.AssociativeMapping<*>>()
        .all { it.complete }

    /**
     * @return the remaining [PropertyMapping.AssociativeMapping] mappings in the table
     */
    fun remainingAssociativeMappings() = this.mappings
        .filterIsInstance<PropertyMapping.AssociativeMapping<*>>()
        .filter { !it.complete }

    /**
     * @return the remaining [PropertyMapping.AssociativeMapping] mappings in the table
     */
    fun remainingPrimitiveAssociativeMappings() = this.mappings
        .filterIsInstance<PropertyMapping.PrimitiveAssociativeMapping<*>>()
        .filter { !it.complete }

}
