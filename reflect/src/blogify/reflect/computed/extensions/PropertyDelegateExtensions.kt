package blogify.reflect.computed.extensions

import blogify.reflect.computed.models.BasicComputedProperty
import blogify.reflect.models.Mapped

import kotlinx.coroutines.runBlocking

/**
 * Creates a [BasicComputedProperty] computed property container
 *
 * @param function the function used to compute the value when the property is resolved
 *
 * @author Benjozork
 */
fun <TMapped : Mapped, TProperty : Any?> TMapped.computed (function: () -> TProperty): BasicComputedProperty<TMapped, TProperty> =
    BasicComputedProperty(this, function)

/**
 * Creates a [BasicComputedProperty] computed property container, which accepts suspending functions.
 * Does not actually run resolutions in parallel; see [blogify.reflect.computed.resolveComputedPropsAsync] for that.
 *
 * @param function the function used to compute the value when the property is resolved
 *
 * @author Benjozork
 */
fun <TMapped : Mapped, TProperty : Any?> TMapped.computedSuspending (function: suspend () -> TProperty): BasicComputedProperty<TMapped, TProperty> =
    BasicComputedProperty(this, { runBlocking { function() } })
