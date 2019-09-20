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