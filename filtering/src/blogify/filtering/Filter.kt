package blogify.filtering

import blogify.reflect.SlicedProperty
import blogify.reflect.extensions.okHandle
import blogify.reflect.getPropValueOnInstance
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap

import kotlin.reflect.KProperty1

abstract class Filter<TMapped : Mapped, TProperty : Any?>(val property: KProperty1<TMapped, TProperty>) {

    init {
        assert(property.okHandle is PropMap.PropertyHandle.Valid) { "cannot create a filter for a property that doesn't have a Valid handle in a safe propMap" }
    }

    abstract infix fun matchesFor(value: TProperty): Boolean

    @Suppress("UNCHECKED_CAST")
    infix fun matchesFor(instance: TMapped): Boolean {
        return when (val valueOnInstance = getPropValueOnInstance(instance, property.name)) {
            is SlicedProperty.Value -> matchesFor(valueOnInstance.value as TProperty)
            is SlicedProperty.NullableValue -> matchesFor(valueOnInstance.value as TProperty)
            else -> false
        }
    }

    class IsValue<TMapped : Mapped, TProperty : Any?> (
        property: KProperty1<TMapped, TProperty>,
        val value: TProperty
    ) : Filter<TMapped, TProperty>(property) {

        override fun matchesFor(value: TProperty) = this.value == value

    }

    class IsNotValue<TMapped : Mapped, TProperty : Any?> (
        property: KProperty1<TMapped, TProperty>,
        val value: TProperty
    ) : Filter<TMapped, TProperty>(property) {

        override fun matchesFor(value: TProperty) = this.value != value

    }

}
