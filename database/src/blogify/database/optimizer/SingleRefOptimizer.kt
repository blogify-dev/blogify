package blogify.database.optimizer

import blogify.common.util.never
import reflectify.extensions.safeKlass
import blogify.database.annotations.table
import blogify.database.binding.SqlBinding
import reflectify.entity.Entity

import org.jetbrains.exposed.sql.*

import kotlin.reflect.KClass

/**
 * Creates a [query][Query] for retrieving both an instance of [TEntity]
 * and all of it's [single reference][SqlBinding.Reference] properties.
 */
@ExperimentalStdlibApi
fun <TEntity : Entity> makeJoinForClass(klass: KClass<TEntity>): ColumnSet {
    val entityTable = klass.table
    val tableSingleRefBindings = entityTable.bindings.filter {
         it is SqlBinding.Reference<TEntity, *> || it is SqlBinding.NullableReference<TEntity, *>
    }

    return tableSingleRefBindings.fold(entityTable as ColumnSet) { acc, binding ->
        val otherTable = binding.property.returnType.safeKlass<Entity>()?.table ?: never
        val aliasedTable = otherTable.alias("${klass.simpleName}->${binding.property.name}")

        acc.leftJoin (
            otherTable = aliasedTable,
            onColumn = { (binding as SqlBinding.HasColumn<*>).column },
            otherColumn = { aliasedTable[otherTable.uuid] }
        )
    }
}
