fun <K : Any, V : Any, R : Any> Map<K, V>.filterThenMapValues (
    predicate: (V) -> Boolean,
    mapper:    (Map.Entry<K, V>) -> R
): Map<K, R> {
    return this.filterValues(predicate).mapValues(mapper)
}

typealias Dto = Map<String, Any?>

