package blogify.reflect

import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok

/**
 * Verifies that a [Mapped] object's [String] properties conform to
 * any [check][check] annotation regexps
 *
 * @author Benjozork
 */
fun Mapped.verify(): Map<PropMap.PropertyHandle.Ok, Boolean> = this.propMap.ok
    .mapKeys { it.value } // Use property handles as keys
    .filterThenMapValues (
        { it.property.returnType.classifier == String::class },
        { (it.value.regexCheck?.let { regex -> (it.value.property.get(this) as String).matches(regex) } ?: true) }
    )
