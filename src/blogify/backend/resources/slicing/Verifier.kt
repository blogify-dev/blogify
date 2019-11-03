package blogify.backend.resources.slicing

import blogify.backend.resources.slicing.models.Mapped

/**
 * Verifies that a [Mapped] object's [String] properties conform to
 * any [check][blogify.backend.annotations.check] annotation regexps
 *
 * @author Benjozork
 */
fun Mapped.verify(): Map<PropertyHandle.Ok, Boolean> = this.cachedPropMap()
    .filterValues { it is PropertyHandle.Ok }
    .mapValues    { it.value as PropertyHandle.Ok }
    .filterValues { it.property.returnType.classifier == String::class }
    .map          { it.value to (it.value.regexCheck?.let { regex -> (it.value.property.get(this) as String).matches(regex) } ?: true) }
    .toMap()