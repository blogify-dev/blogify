package blogify.reflect.analysis.models

import blogify.reflect.models.Mapped
import blogify.reflect.analysis.models.metadata.ClassMetadata
import blogify.reflect.analysis.models.metadata.Metadata
import blogify.reflect.analysis.models.metadata.MetadataProvider

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class ClassDescriptor(val klass: KClass<out Mapped>) : ElementDescriptor<KClass<out Mapped>>() {

    override val storedMetadata = mutableMapOf<KClass<out Metadata<KClass<out Mapped>>>, Metadata<KClass<out Mapped>>>()

    @Suppress("UNCHECKED_CAST")
    val propertyDescriptors = klass.declaredMemberProperties.associateWith { PropertyDescriptor(it) }
        .toMutableMap() as MutableMap<KProperty1<Mapped, Any?>, PropertyDescriptor>

    inline fun <reified TMetadata : ClassMetadata> getOrMake (
        provider: MetadataProvider<TMetadata, KClass<out Mapped>>
    ): TMetadata {
        return storedMetadata.getOrPut(TMetadata::class, { provider.provideFor(klass) }) as TMetadata
    }

}
