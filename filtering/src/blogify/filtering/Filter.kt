package blogify.filtering

import blogify.reflect.models.Mapped

import kotlin.reflect.KProperty1

abstract class Filter<TMapped : Mapped, TProperty : Any?>(property: KProperty1<TMapped, TProperty>) {

    abstract infix fun matchesFor(value: TProperty): Boolean

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
