package blogify.backend.database.optimizer

import blogify.backend.annotations.table
import blogify.backend.database.binding.SqlBinding
import blogify.common.util.never
import blogify.reflect.entity.Entity
import blogify.reflect.extensions.safeKlass

import org.jetbrains.exposed.sql.*

import kotlin.reflect.KClass

/**
 * Creates a [query][org.jetbrains.exposed.sql.Query] for retrieving both an instance of [TResource]
 * and all of it's [single reference][blogify.backend.database.binding.SqlBinding.Reference] properties.
 */
fun <TResource : Entity> makeJoinForClass(klass: KClass<TResource>): ColumnSet {
    val resourceTable = klass.table
    val tableSingleRefBindings = resourceTable.bindings.filter {
         it is SqlBinding.Reference<TResource, *> || it is SqlBinding.NullableReference<TResource, *>
    }

    return tableSingleRefBindings.fold(resourceTable as ColumnSet) { acc, binding ->
        val otherTable = binding.property.returnType.safeKlass<Entity>()?.table ?: never
        val aliasedTable = otherTable.alias("${klass.simpleName}->${binding.property.name}")

        acc.leftJoin (
            otherTable = aliasedTable,
            onColumn = { (binding as SqlBinding.HasColumn<*>).column },
            otherColumn = { aliasedTable[otherTable.uuid] }
        )
    }
}
