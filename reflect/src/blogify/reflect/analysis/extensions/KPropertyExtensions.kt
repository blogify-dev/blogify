package blogify.reflect.analysis.extensions

import blogify.reflect.extensions.klass
import blogify.reflect.models.Mapped

import kotlin.reflect.KProperty1

val KProperty1<out Mapped, *>.descriptor get() =
    this.klass.descriptor.propertyDescriptors[this]!!
