package blogify.filtering.metadata

import blogify.reflect.extensions.isPrimitive
import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.analysis.models.metadata.PropertyMetadata

import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Metadata about filtering entity properties.
 *
 * @property isFilterable whether or not the property can be used to filter entities of it's class' type
 *
 * @author Benjozork
 */
class FilteringMetadata (
    val isFilterable: Boolean
) : PropertyMetadata {

    object Provider : MetadataProvider<FilteringMetadata, KProperty<*>> {

        override fun provideFor(element: KProperty<*>): FilteringMetadata {
            return FilteringMetadata (
                isFilterable = element.isFilterable
            )
        }

        val KProperty<*>.isFilterable: Boolean
            get() {
                val isOfCompatibleType = this.returnType.isPrimitive()

                return isOfCompatibleType && this.findAnnotation<NotFilterable>() == null
            }

    }

}
