package blogify.backend.database.extensions

import blogify.backend.database.models.ResourceTable
import blogify.reflect.extensions.klass

/**
 * Returns the class of the type of Resource this table stores. Requires at least one [blogify.backend.database.binding.SqlBinding] to be
 * declared.
 *
 * @author Benjozork
 */
val ResourceTable<*>.klass get() = this.bindings.first().property.klass
