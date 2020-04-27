package reflect

import filterThenMapValues
import reflect.models.Mapped
import reflect.models.PropMap
import reflect.models.extensions.ok

/**
 * Verifies that a [Mapped] object's [String] properties conform to
 * any [check][annotations.check] annotation regexps
 *
 * @author Benjozork
 */
fun Mapped.verify(): Map<PropMap.PropertyHandle.Ok, Boolean> = this.cachedPropMap().ok()
    .mapKeys { it.value } // Use property handles as keys
    .filterThenMapValues (
        { it.property.returnType.classifier == String::class },
        { (it.value.regexCheck?.let { regex -> (it.value.property.get(this) as String).matches(regex) } ?: true) }
    )
