package blogify.reflect.analysis.impl

import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.analysis.models.metadata.PropertyMetadata

import kotlin.reflect.KProperty

class BaseMetadata : PropertyMetadata {

    object Provider : MetadataProvider<BaseMetadata, KProperty<*>> {

        override fun provideFor(element: KProperty<*>): BaseMetadata =
            BaseMetadata()

    }

}
