package blogify.reflect.computed.models

import blogify.reflect.models.Mapped

abstract class ComputedPropContainer <TMapped : Mapped, TProperty : Any?> {

    abstract val obj: TMapped

    var resolution: Resolution = Resolution.Unresolved

    var wasResolveAttempted = false

    sealed class Resolution {
        class Value<TProperty : Any?>(val value: TProperty) : Resolution()

        object Undefined : Resolution()

        object Unresolved : Resolution()

        override fun equals(other: Any?): Boolean {
            return if (this is Value<*> && other is Value<*>)
                this.value == other.value
            else false
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

}
