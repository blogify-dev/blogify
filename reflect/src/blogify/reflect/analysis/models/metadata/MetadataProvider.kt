package blogify.reflect.analysis.models.metadata

import kotlin.reflect.KAnnotatedElement

interface MetadataProvider<TMetadata : Metadata<TElement>, TElement : KAnnotatedElement> {

    fun provideFor(element: TElement): TMetadata

}
