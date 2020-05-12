package blogify.reflect.computed.extensions

import blogify.reflect.computed.models.BasicComputedProperty
import blogify.reflect.models.Mapped

fun <TMapped : Mapped, TProperty : Any?> TMapped.computed(function: (TMapped) -> TProperty): BasicComputedProperty<TMapped, TProperty> =
        BasicComputedProperty(this, function)
