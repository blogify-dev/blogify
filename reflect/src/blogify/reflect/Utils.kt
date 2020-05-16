package blogify.reflect

import blogify.reflect.models.PropMap

fun <K : Any, V : Any, R : Any> Map<K, V>.filterThenMapValues (
    predicate: (V) -> Boolean,
    mapper:    (Map.Entry<K, V>) -> R
): Map<K, R> {
    return this.filterValues(predicate).mapValues(mapper)
}

typealias Dto = Map<String, Any?>

typealias MappedData = Map<PropMap.PropertyHandle.Ok, Any?>
