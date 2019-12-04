package blogify.backend.resources.computed.models

import blogify.backend.resources.models.Resource

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Computed

abstract class ComputedPropertyDelegate<A : Any> : ReadOnlyProperty<Resource, A> {

    abstract override fun getValue(thisRef: Resource, property: KProperty<*>): A

}
