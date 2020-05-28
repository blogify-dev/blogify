package blogify.reflect.analysis.models

import blogify.reflect.analysis.models.metadata.Metadata
import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.analysis.models.metadata.PropertyMetadata

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class PropertyDescriptor(val element: KProperty<*>) : ElementDescriptor<PropertyMetadata, KProperty<*>>() {

    override val storedMetadata = mutableMapOf<KClass<out Metadata<KProperty<*>>>, Metadata<KProperty<*>>>()

    inline fun <reified TMetadata : Metadata<KProperty<*>>> getOrMake (
        provider: MetadataProvider<TMetadata, KProperty<*>>
    ): TMetadata {
        return storedMetadata.getOrPut(TMetadata::class, { provider.provideFor(element) }) as TMetadata
    }

}
