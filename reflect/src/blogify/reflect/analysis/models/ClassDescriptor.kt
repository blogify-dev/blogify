package blogify.reflect.analysis.models

import blogify.reflect.analysis.models.metadata.Metadata
import blogify.reflect.models.Mapped

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class ClassDescriptor(val klass: KClass<*>) : ElementDescriptor<KClass<*>>() {

    override val storedMetadata = mutableMapOf<KClass<out Metadata<KClass<*>>>, Metadata<KClass<*>>>()

    @Suppress("UNCHECKED_CAST")
    val propertyDescriptors = klass.declaredMemberProperties.associateWith { PropertyDescriptor(it) }
        .toMutableMap() as MutableMap<KProperty1<Mapped, Any?>, PropertyDescriptor>

}
