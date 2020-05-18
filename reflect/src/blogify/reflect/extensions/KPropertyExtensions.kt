package blogify.reflect.extensions

import blogify.reflect.propMap
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

import com.andreapivetta.kolor.red

@Suppress("UNCHECKED_CAST")
val <TClass : Any> KProperty1<TClass, *>.klass: KClass<out TClass> get() =
    this.getter.parameters.first().type.safeKlass() ?: error("can't get class of type projection".red())

/**
 * Returns the [PropMap.PropertyHandle] for this property.
 */
val <TResource : Mapped, TProperty : Any?> KProperty1<TResource, TProperty>.handle: PropMap.PropertyHandle get() =
    this.klass.propMap.map[this.name] ?: error("no handle for property '${this.name}' of class ${this.klass.simpleName}".red())

/**
 * Returns the [PropMap.PropertyHandle.Ok] for this property, if available. Does not use an unsafe propmap.
 */
val <TResource : Mapped, TProperty : Any?> KProperty1<TResource, TProperty>.okHandle: PropMap.PropertyHandle.Ok? get() =
    this.klass.propMap.ok[this.name]
