package blogify.backend.util

import java.util.concurrent.ConcurrentHashMap

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

/**
 * Returns with only the entries whose key is not `null`.
 */
@Suppress("UNCHECKED_CAST")
fun <K : Any, V : Any> Map<K, V?>.withoutNullValues() =
    this.filterValues { it != null }.toMap() as Map<K, V>

fun <K : Any, V : Any> concurrentMapOf(vararg entries: Pair<K, V>) = ConcurrentHashMap<K, V>(entries.toMap())
