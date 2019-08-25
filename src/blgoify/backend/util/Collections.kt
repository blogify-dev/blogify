package blgoify.backend.util

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