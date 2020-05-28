package blogify.reflect.analysis.models

import blogify.reflect.analysis.models.metadata.ClassMetadata
import blogify.reflect.analysis.models.metadata.Metadata

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class ClassDescriptor(val klass: KClass<*>) : ElementDescriptor<ClassMetadata, KClass<*>>() {

    override val storedMetadata = mutableMapOf<KClass<out Metadata<KClass<*>>>, Metadata<KClass<*>>>()

    var propertyDescriptors = klass.declaredMemberProperties.associateWith { PropertyDescriptor(it) }
        .toMutableMap()

}
