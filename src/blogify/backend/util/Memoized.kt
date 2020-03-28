package blogify.backend.util

import java.util.concurrent.ConcurrentHashMap

class Memoized<T, R>(val function: (T) -> R) : (T) -> R {
    private val store = ConcurrentHashMap<T, R>()

    override fun invoke(key: T): R = store.getOrPut(key, { function(key) })
}

fun <T : Any, R : Any> ((T) -> R).memoized(): (T) -> R = Memoized(this)
