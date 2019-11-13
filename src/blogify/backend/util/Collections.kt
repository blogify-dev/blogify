package blogify.backend.util

/**
 * Allows to specify a function to execute depending on whether a collection has exactly one item, multiple items or no items.
 */
suspend fun <Ct, Rt> Iterable<Ct>.foldForOne (
    one:      suspend (Ct) -> Rt,
    multiple: suspend (Iterable<Ct>) -> Rt,
    none:     suspend () -> Rt
) : Rt {
    val iter = iterator()
    if (!iter.hasNext()) {
        return none()
    }
    val e = iter.next()
    return if (iter.hasNext()) {
        multiple(this)
    } else {
        one(e)
    }
}

fun <T : Any, R : Any> Collection<T>.filterMap(predicate: (T) -> Boolean, mapper: (T) -> R): Collection<R> {
    return this.filter(predicate).map(mapper)
}

fun <K : Any, V : Any, R : Any> Map<K, V>.filterThenMapKeys (
    predicate: (K) -> Boolean,
    mapper:    (Map.Entry<K, V>) -> R
): Map<R, V> {
    return this.filterKeys(predicate).mapKeys(mapper)
}
fun <K : Any, V : Any, R : Any> Map<K, V>.filterThenMapValues (
    predicate: (V) -> Boolean,
    mapper:    (Map.Entry<K, V>) -> R
): Map<K, R> {
    return this.filterValues(predicate).mapValues(mapper)
}
