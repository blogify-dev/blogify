package blogify.backend.resources.reflect

import blogify.backend.resources.models.Resource
import blogify.backend.annotations.NoSlice
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap

/**
 * Represents the result of [getPropValueOnInstance].
 *
 * @author Benjozork
 */
sealed class SlicedProperty(val name: String) {
    /**
     * Represents a successfully fetched property value, available in [value]
     *
     * @param name  the name of the requested (and in this case returned) property
     * @param value the value that was fetched
     */
    class Value(name: String, val value: Any) : SlicedProperty(name)

    /**
     * Represents a successfully fetched nullable property value, available in [value]
     *
     * @param name  the name of the requested (and in this case returned) property
     * @param value the nullable value that was fetched
     */
    class NullableValue(name: String, val value: Any?) : SlicedProperty(name)

    /**
     * Represents a property that was not found on the instance class
     *
     * @param name the name of the requested property
     */
    class NotFound(name: String) : SlicedProperty(name)

    /**
     * Represents a property that was found on the specific instance class, but could not be fetched due to security policy reasons
     *
     * @param name the name of the requested property
     */
    class AccessNotAllowed(name: String) : SlicedProperty(name)
}

/**
 * Reads a property from an instance of [M] with [a certain name][propertyName] using reflection
 *
 * Shamelessly stolen from: [https://stackoverflow.com/a/35539628]
 *
 * @param instance     instance of [M] to read property from
 * @param propertyName name of the property to read
 *
 * @return a [SlicedProperty] representing the result of the query. Can be either [SlicedProperty.Value] for success,
 * [SlicedProperty.NotFound] for an unknown property or [SlicedProperty.AccessNotAllowed] for a property that cannot
 * be accessed for security policy reasons (in which case, of course, that incident would be reported).
 *
 * @author hamza1311, Benjozork
 */
@Suppress("UNCHECKED_CAST")
private fun <M : Mapped> getPropValueOnInstance(instance: M, propertyName: String): SlicedProperty {
    return instance.cachedPropMap().map
        .entries.firstOrNull { (name, _) -> name == propertyName }
        ?.value?.let { handle ->
            return when (handle) {
                is PropMap.PropertyHandle.Ok -> { // Handle is ok, proceed
                    if (handle.property.returnType.isMarkedNullable) {
                        SlicedProperty.NullableValue(propertyName, handle.property.get(instance))
                    } else {
                        SlicedProperty.Value(propertyName, handle.property.get(instance))
                    }
                }
                is PropMap.PropertyHandle.AccessDenied -> SlicedProperty.AccessNotAllowed(handle.name) // Handle is denied
            }
        } ?: SlicedProperty.NotFound(propertyName) // No property handle found
}

/**
 * Slices a [resources[Resource] with a set of provided properties that should be kept
 *
 * @receiver the [resource][Resource] to be sliced
 *
 * @param selectedPropertyNames the properties that should be kept on the returned [resources][Resource]
 *
 * @return a list of [maps][Map] containing [resources][Resource] with only the provided properties on them
 *
 * @author hamza1311, Benjozork
 */
fun <M : Mapped> M.slice(selectedPropertyNames: Set<String>): Map<String, Any?> {

    val selectedPropertiesSanitized = selectedPropertyNames.toMutableSet().apply {
        removeIf { it == "uuid" || it == "UUID" }
        add("uuid")
    }

    val unknownProperties = mutableSetOf<String>()
    val accessDeniedProperties = mutableSetOf<String>()

    return selectedPropertiesSanitized.associateWith { propName ->
        when (val result = getPropValueOnInstance(this, propName)) {
            is SlicedProperty.Value            -> result.value
            is SlicedProperty.NullableValue    -> result.value
            is SlicedProperty.NotFound         -> unknownProperties += result.name
            is SlicedProperty.AccessNotAllowed -> accessDeniedProperties += result.name
        }
    }.filter {
        it.value != Unit // Don't return invalid values
    }.toMutableMap().apply {
        if (unknownProperties.isNotEmpty())
            this["_notFound"] = unknownProperties // Add unknown properties to final map
        if (accessDeniedProperties.isNotEmpty())
            this["_accessDenied"] = accessDeniedProperties // Add unknown properties to final map
    }.toMap()
}

/**
 * Slices a resource with all of its properties except ones annotated with [@noslice][NoSlice]
 *
 * @receiver the [resource][Resource] to be sliced
 *
 * @author Benjozork
 */
fun <M : Mapped> M.sanitize(): Map<String, Any?> {
    val sanitizedClassProps = this::class.cachedPropMap()
        .asSequence()
        .filter { it.value is PropMap.PropertyHandle.Ok }
        .map    { it.key }
        .toSet()

    return this.slice(sanitizedClassProps)
}
