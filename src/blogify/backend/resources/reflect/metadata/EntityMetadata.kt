package blogify.backend.resources.reflect.metadata

import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.analysis.models.metadata.PropertyMetadata
import blogify.reflect.annotations.Hidden

import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Metadata for blogify entities.
 *
 * @author Benjozork
 *
 * @property hidden is `true` when this element should be prohibited from appearing in DTOs or be accepted
 *                  during entity creation or update
 */
class EntityMetadata (
    val hidden: Boolean
) : PropertyMetadata {

    object Provider : MetadataProvider<EntityMetadata, KProperty<*>> {

        override fun provideFor(element: KProperty<*>): EntityMetadata =
            EntityMetadata (
                hidden = element.isHidden
            )

        private val KProperty<*>.isHidden get() = this.findAnnotation<Hidden>() != null

    }

}
