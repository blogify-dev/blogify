package blogify.reflect.computed.models

import blogify.reflect.models.Identified

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("Please use new computed properties instead.")
annotation class Computed

abstract class ComputedPropertyDelegate<A : Any> : ReadOnlyProperty<Identified, A> {

    abstract override fun getValue(thisRef: Identified, property: KProperty<*>): A

}
