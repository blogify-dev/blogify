package blogify.reflect.computed.models

import blogify.reflect.models.Mapped

class BasicComputedProperty<TMapped : Mapped, TProperty : Any?> (
    override val obj: TMapped,
    val function: (TMapped) -> TProperty
) : ComputedPropContainer<TMapped, TProperty>() {

    override var resolved: TProperty? = null

}
