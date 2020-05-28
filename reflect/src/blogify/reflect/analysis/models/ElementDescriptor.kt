package blogify.reflect.analysis.models

import blogify.reflect.analysis.models.metadata.Metadata

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass

abstract class ElementDescriptor<TElement : KAnnotatedElement> {

    abstract val storedMetadata: MutableMap<KClass<out Metadata<TElement>>, Metadata<TElement>>

}
