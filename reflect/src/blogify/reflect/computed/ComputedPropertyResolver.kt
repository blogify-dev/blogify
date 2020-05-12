package blogify.reflect.computed

import blogify.reflect.SlicedProperty
import blogify.reflect.computed.models.BasicComputedProperty
import blogify.reflect.computed.models.ComputedPropContainer
import blogify.reflect.extensions.subTypeOf
import blogify.reflect.getPropValueOnInstance
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.valid
import blogify.reflect.propMap

/**
 * Evaluates the computed properties of [instance] and puts the value inside the [container][ComputedPropContainer] of each property.
 *
 * This process is NOT thread-safe. This should only be called once during the lifetime of [instance].
 *
 * @param instance       the [mapped object][Mapped] to work on
 * @param customResolver a function that can handle custom subclasses of [ComputedPropContainer] if a library-provided subclass isn't used.
 *
 * @return a map of [valid handles][PropMap.PropertyHandle.Valid] to [their container instances][ComputedPropContainer].
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> resolveComputedProps (
    instance: TMapped,
    customResolver: (ComputedPropContainer<TMapped, *>) -> Any? =
            { error("unknown computed property container was found, but no custom resolver was provided") }
): Map<PropMap.PropertyHandle.Valid, ComputedPropContainer<TMapped, Any>> {
    // Find all prop containers in instance
    val allPropContainers = findAllPropContainers(instance)

    return allPropContainers.map { (handle, container) ->
        val value = doResolveComputedProperty(instance, container, customResolver)

         container.resolution = ComputedPropContainer.Resolution.Value(value)

        if (value != null)
            handle to container
        else null
    }.filterNotNull().toMap()
}

/**
 * Find *all* properties with a type `<:` [ComputedPropContainer]
 *
 * @param instance the instance of [TMapped] to work on
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
private fun <TMapped : Mapped> findAllPropContainers(instance: TMapped) =
    instance.propMap.valid
        .filter { (_, h) -> h.property.returnType subTypeOf ComputedPropContainer::class }
        .values.map { getPropValueOnInstance(instance, it.name, keepComputedContainers = true) }
        .filterIsInstance<SlicedProperty.Value>()
        .map { it.handle to it.value as ComputedPropContainer<TMapped, Any> }
        .toMap()

/**
 * Attempts to find a value for [container] using a default implementation for library-provided subclasses of [ComputedPropContainer] and
 * [customResolver] for custom subclasses.
 *
 * @param instance       the [mapped object][Mapped] to work on
 * @param container      the [container][ComputedPropContainer] to resolve a value for
 * @param customResolver a function that can handle custom subclasses of [ComputedPropContainer] if a library-provided subclass isn't used.
 *                       An error is thrown if neither this implementation or [customResolver] returned a non-null value.
 *
 * @author Benjozork
 */
private fun <TMapped : Mapped> doResolveComputedProperty (
    instance: TMapped,
    container: ComputedPropContainer<TMapped, *>,
    customResolver: (ComputedPropContainer<TMapped, *>) -> Any?
): Any? {
    return when (container) {
        is BasicComputedProperty<TMapped, *> -> container.function(instance)
        else -> customResolver(container)
    }
}
