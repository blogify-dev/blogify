package computed.models

import NotResouce

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Computed

abstract class ComputedPropertyDelegate<A : Any> : ReadOnlyProperty<NotResouce, A> {

    abstract override fun getValue(thisRef: NotResouce, property: KProperty<*>): A

}
