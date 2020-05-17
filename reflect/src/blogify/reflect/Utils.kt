package blogify.reflect

import blogify.reflect.models.PropMap

fun <K : Any, V : Any, R : Any> Map<K, V>.filterThenMapValues (
    predicate: (V) -> Boolean,
    mapper:    (Map.Entry<K, V>) -> R
): Map<K, R> {
    return this.filterValues(predicate).mapValues(mapper)
}

/**
 * A map of property names to associated values
 */
typealias Dto = Map<String, Any?>

/**
 * A map of [property handles][PropMap.PropertyHandle.Ok] to associated values
 */
typealias MappedData = Map<PropMap.PropertyHandle.Ok, Any?>
