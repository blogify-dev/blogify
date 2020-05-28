package blogify.reflect.analysis.models

import blogify.reflect.analysis.models.metadata.Metadata

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass

abstract class ElementDescriptor<TMetadataType : Metadata<TElement>, TElement : KAnnotatedElement> {

    abstract val storedMetadata: MutableMap<KClass<out Metadata<TElement>>, Metadata<TElement>>

}
