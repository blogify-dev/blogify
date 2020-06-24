package blogify.reflect.entity.metadata

import blogify.reflect.analysis.models.metadata.ClassMetadata
import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.annotations.search.NoSearch
import blogify.reflect.models.Mapped

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Metadata for blogify entities.
 *
 * @author Benjozork
 *
 * @property isSearchable is `false` when this element type cannot have a search performed on it
 */
class EntityClassMetadata (
    val isSearchable: Boolean
) : ClassMetadata {

    object Provider : MetadataProvider<EntityClassMetadata, KClass<out Mapped>> {

        override fun provideFor(element: KClass<out Mapped>): EntityClassMetadata =
            EntityClassMetadata (
                isSearchable = element.isSearchable
            )

        private val KClass<out Mapped>.isSearchable get() = this.findAnnotation<NoSearch>() == null

    }

}
