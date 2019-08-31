package blogify.backend.util

fun <T> Iterable<T>.singleOrNullOrError(): T? {
    when (this) {
        is List -> return when {
            size == 1 -> this[0]
            size > 1  -> error("Collection has multiple elements")
            else      -> null
        }
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext())
                return null
            val single = iterator.next()
            if (iterator.hasNext())
                error("Collection has multiple elements")
            return single
        }
    }
}

/**
 * Allows to specify a function to execute depending on whether a collection has exactly one item, multiple items or no items.
 */
suspend fun <Ct, Rt> Collection<Ct>.foldForOne (
    one:      suspend (Ct) -> Rt,
    multiple: suspend (Collection<Ct>) -> Rt,
    none:     suspend () -> Rt
) : Rt {
    return when  {
        size == 1    -> one(first())
        isNotEmpty() -> multiple(this)
        isEmpty()    -> none()
        else         -> error("impossible state")
    }
}