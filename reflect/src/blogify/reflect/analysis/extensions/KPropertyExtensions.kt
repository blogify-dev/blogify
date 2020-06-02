package blogify.reflect.analysis.extensions

import blogify.reflect.analysis.models.PropertyDescriptor
import blogify.reflect.extensions.klass
import blogify.reflect.models.Mapped

import kotlin.reflect.KProperty1

val KProperty1<out Mapped, Any?>.descriptor: PropertyDescriptor get() =
    this.klass.descriptor.propertyDescriptors[this]!!
