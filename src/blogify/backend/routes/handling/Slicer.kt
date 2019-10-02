package blogify.backend.routes.handling

import blogify.backend.resources.models.Resource
import blogify.backend.util.noslice

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

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
 * Reads a property from an instance of [T] with [a certain name][propertyName] using reflection
 *
 * Shamelessly stolen from: [https://stackoverflow.com/a/35539628]
 *
 * @param instance     instance of [T] to read property from
 * @param propertyName name of the property to read
 *
 * @return the value of the property [propertyName] on [instance] or `null` if that property doesn't exist on [instance]
 *
 * @author hamza1311, Benjozork
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Resource, R : Any> getPropValueOnInstance(instance: T, propertyName: String): SlicedProperty {
    return instance::class.declaredMemberProperties
        .firstOrNull {
            it.name == propertyName
        }?.let { prop ->
            return if (prop.annotations.any { it.annotationClass == noslice::class }) {
                // Property has @noslice annotation
                SlicedProperty.AccessNotAllowed(propertyName)
            } else {
                // Property is found and access is allowed
                SlicedProperty.Value(propertyName, (prop as KProperty1<T, R>).get(instance))
            }
            // Property is not found
        } ?: SlicedProperty.NotFound(propertyName)
}

/**
 * Slices a [resources][Resource] with a set of provided properties that should be kept
 *
 * @param resource              the [resources][Resource] to be sliced
 * @param selectedPropertyNames the properties that should be kept on the returned [resources][Resource]
 *
 * @return a list of [maps][Map] containing [resources][Resource] with only the provided properties on them
 *
 * @author hamza1311, Benjozork
 */
fun <R : Resource> R.slice(selectedPropertyNames: Set<String>): Map<String, Any> {

    val selectedPropertiesWithoutUUID = selectedPropertyNames.toMutableSet().apply {
        removeIf { it == "uuid" || it == "UUID" }
    }

    val unknownProperties = mutableSetOf<String>()
    val accessDeniedProperties = mutableSetOf<String>()

    return selectedPropertiesWithoutUUID.associateWith { propName ->
        when (val result = getPropValueOnInstance<Resource, Any>(this, propName)) {
            is SlicedProperty.Value            -> result.value
            is SlicedProperty.NotFound         -> unknownProperties += result.name
            is SlicedProperty.AccessNotAllowed -> accessDeniedProperties += result.name
        }
    }.filter {
        it.value != Unit // Don't return invalid values
    }.toMutableMap().apply {
        this["uuid"] = (getPropValueOnInstance<Resource, String>(this@slice, "uuid") as SlicedProperty.Value).value
        if (unknownProperties.isNotEmpty())
            this["_notFound"] = unknownProperties // Add unknown properties to final map
        if (accessDeniedProperties.isNotEmpty())
            this["_accessDenied"] = accessDeniedProperties // Add unknown properties to final map
    }.toMap()
}
