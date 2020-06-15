package blogify.reflect.entity.metadata

import blogify.common.util.letCatchingOrNull
import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.analysis.models.metadata.PropertyMetadata
import blogify.reflect.annotations.Hidden
import blogify.reflect.entity.annotations.NotUpdatable
import blogify.reflect.entity.typing.Kind
import blogify.reflect.entity.typing.kind

import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Metadata for blogify entities.
 *
 * @author Benjozork
 *
 * @property isVisible is `false` when this element should be prohibited from appearing in DTOs or be accepted
 *                  during entity creation or update
 */
class EntityMetadata (
    val isVisible: Boolean,
    val isUpdatable: Boolean,
    val kind: Kind?
) : PropertyMetadata {

    object Provider : MetadataProvider<EntityMetadata, KProperty<*>> {

        override fun provideFor(element: KProperty<*>): EntityMetadata =
            EntityMetadata (
                isVisible = !element.isHidden,
                isUpdatable = !element.isUpdatable && !element.isHidden,
                kind = letCatchingOrNull { element.kind }
            )

        private val KProperty<*>.isHidden get() = this.findAnnotation<Hidden>() != null

        private val KProperty<*>.isUpdatable get() = this.findAnnotation<NotUpdatable>() != null

    }

}
