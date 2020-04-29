package reflect.extensions

import reflect.cachedPropMap
import reflect.models.Mapped
import reflect.models.PropMap
import reflect.models.extensions.ok
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

//import com.andreapivetta.kolor.red

private fun String.red(): String {
    return this
}

@Suppress("UNCHECKED_CAST")
private val <TClass : Any> KProperty1<TClass, *>.klass: KClass<out TClass> get() =
    this.getter.parameters.first().type.classifier as? KClass<out TClass> ?: error("can't get class of type projection".red())

/**
 * Returns the [PropMap.PropertyHandle] for this property.
 */
val <TResource : Mapped, TProperty : Any> KProperty1<TResource, TProperty>.handle: PropMap.PropertyHandle get() =
    this.klass.cachedPropMap().map[this.name] ?: error("no handle for property '${this.name}' of class ${this.klass.simpleName}".red())

/**
 * Returns the [PropMap.PropertyHandle.Ok] for this property, if available. Does not use an unsafe propmap.
 */
val <TResource : Mapped, TProperty : Any> KProperty1<TResource, TProperty>.okHandle: PropMap.PropertyHandle.Ok? get() =
    this.klass.cachedPropMap().ok()[this.name]