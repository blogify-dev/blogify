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

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Asynchronously evaluates the computed properties of [instance] and puts the value inside the [container][ComputedPropContainer] of each property.
 *
 * @param instance                 the [mapped object][Mapped] to work on
 * @param coroutineContext         a custom [CoroutineContext] to use for starting the resolution jobs
 * @param customResolver           a function that can handle custom subclasses of [ComputedPropContainer] if a library-provided subclass isn't used.
 * @param markUndefinedOnException whether or not a [container][ComputedPropContainer] should have the value of [ComputedPropContainer.Resolution.Undefined]
 *                                 when there is an exception while resolving it
 *
 * @return a map of [valid handles][PropMap.PropertyHandle.Valid] to [their container instances][ComputedPropContainer].
 *
 * @author Benjozork
 */
suspend fun <TMapped : Mapped> resolveComputedPropsAsync (
    instance: TMapped,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    markUndefinedOnException: Boolean = false,
    customResolver: (ComputedPropContainer<TMapped, *>) -> Any? =
        { error("unknown computed property container was found, but no custom resolver was provided") }
): Map<PropMap.PropertyHandle.Valid, ComputedPropContainer<TMapped, Any>> {
    // Find all prop containers in instance
    val allPropContainers = findAllPropContainers(instance)

    val containerJobs = allPropContainers.map { (_, container) ->
        GlobalScope.async(coroutineContext) { doResolveComputedProperty(container, customResolver) }
    }

    val completedJobs = containerJobs.awaitAll()

    val containersAndValues = (allPropContainers.entries zip completedJobs)

    return containersAndValues.mapNotNull { (handleAndContainer, valueAndException) ->
        val (handle, container) = handleAndContainer
        val (value, exception) = valueAndException

        container.resolution = when {
            value != null -> ComputedPropContainer.Resolution.Value(value)
            markUndefinedOnException -> ComputedPropContainer.Resolution.Undefined
            else -> throw IllegalStateException("exception occurred during computed property resolution and markUndefinedOnException is false", exception)
        }

        if (value != null)
            handle to container
        else null
    }.toMap()
}

/**
 * Evaluates the computed properties of [instance] and puts the value inside the [container][ComputedPropContainer] of each property.
 *
 * @param instance                 the [mapped object][Mapped] to work on
 * @param customResolver           a function that can handle custom subclasses of [ComputedPropContainer] if a library-provided subclass isn't used.
 * @param markUndefinedOnException whether or not a [container][ComputedPropContainer] should have the value of [ComputedPropContainer.Resolution.Undefined]
 *                                 when there is an exception while resolving it
 *
 * @return a map of [valid handles][PropMap.PropertyHandle.Valid] to [their container instances][ComputedPropContainer].
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> resolveComputedProps (
    instance: TMapped,
    markUndefinedOnException: Boolean = false,
    customResolver: (ComputedPropContainer<TMapped, *>) -> Any? =
            { error("unknown computed property container was found, but no custom resolver was provided") }
): Map<PropMap.PropertyHandle.Valid, ComputedPropContainer<TMapped, Any>> {
    // Find all prop containers in instance
    val allPropContainers = findAllPropContainers(instance)

    return allPropContainers.map { (handle, container) ->
        val (value, exception) = doResolveComputedProperty(container, customResolver)

        container.resolution = when {
            value != null -> ComputedPropContainer.Resolution.Value(value)
            markUndefinedOnException -> ComputedPropContainer.Resolution.Undefined
            else -> throw IllegalStateException("exception occurred during computed property resolution and markUndefinedOnException is false", exception)
        }

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
 * @param container      the [container][ComputedPropContainer] to resolve a value for
 * @param customResolver a function that can handle custom subclasses of [ComputedPropContainer] if a library-provided subclass isn't used.
 *                       An error is thrown if neither this implementation or [customResolver] returned a non-null value.
 *
 * @author Benjozork
 */
private fun <TMapped : Mapped> doResolveComputedProperty (
    container: ComputedPropContainer<TMapped, *>,
    customResolver: (ComputedPropContainer<TMapped, *>) -> Any?
): Pair<Any?, Exception?> {
    return when (container) {
        is ComputedPropContainer.AutomaticallyResolvable -> try {
            container.resolve() to null
        } catch (e: Exception) {
            null to e
        }
        else -> try {
            customResolver(container) to null
        } catch (e: Exception) {
            null to e
        }
    }
}
