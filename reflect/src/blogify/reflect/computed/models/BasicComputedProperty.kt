package blogify.reflect.computed.models

import blogify.reflect.models.Mapped

/**
 * A simple [computed property container][ComputedPropContainer] that simply emits a value using a function
 *
 * @author Benjozork
 */
class BasicComputedProperty<TMapped : Mapped, TProperty : Any?> (
    override val obj: TMapped,
    val function: () -> TProperty
) : ComputedPropContainer<TMapped, TProperty>()
