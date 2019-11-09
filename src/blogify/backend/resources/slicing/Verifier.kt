package blogify.backend.resources.slicing

import blogify.backend.resources.slicing.models.Mapped
import blogify.backend.util.filterThenMapValues

/**
 * Verifies that a [Mapped] object's [String] properties conform to
 * any [check][blogify.backend.annotations.check] annotation regexps
 *
 * @author Benjozork
 */
fun Mapped.verify(): Map<PropertyHandle.Ok, Boolean> = this.cachedPropMap().okHandles()
    .mapKeys { it.value } // Use property handles as keys
    .filterThenMapValues (
        { it.property.returnType.classifier == String::class },
        { (it.value.regexCheck?.let { regex -> (it.value.property.get(this) as String).matches(regex) } ?: true) }
    )
