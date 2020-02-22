package blogify.backend.util

/**
 * A simple [MutableMap]-based cache
 *
 * @author Benjozork
 */
open class MapCache<K : Any, V : Any> {

    private val map = mutableMapOf<K, V>()

    protected open operator fun get(key: K): V? = map[key]

    protected operator fun set(key: K, value: V) = map.put(key, value)

    private suspend fun getOrElse(key: K, createNewValue: suspend () -> V): V {
        val attempt = this[key]
        return if (attempt != null) attempt
        else {
            val newValue = createNewValue()
            this[key] = newValue
            return newValue
        }
    }

    /**
     * Returns the value cached with key [key], or the result of [createNewValue] if not found.
     *
     * This function will cast any found value to [D].
     *
     * @param D              the type of the value to be obtained
     * @param key            the [key][K] to use for lookup and insertion
     * @param createNewValue the function used to create a missing value (also inserts it in the cache)
     *
     * @return an [Sr] of [D]
     */
    @Suppress("UNCHECKED_CAST")
    open fun <D : V> findOr(key: K, createNewValue: () -> D): Sr<D>
            =  WrapBlocking { this@MapCache.getOrElse(key, { createNewValue() }) as D }

    /**
     * Returns the value cached with key [key], or the result of [createNewValue] if not found.
     *
     * This function will cast any found value to [D].
     *
     * @param D              the type of the value to be obtained
     * @param key            the [key][K] to use for lookup and insertion
     * @param createNewValue the suspending function used to create a missing value (also inserts it in the cache)
     *
     * @return an [Sr] of [D]
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun <D : V> findOrAsync(key: K, createNewValue: suspend () -> D): Sr<D>
            = Wrap { this.getOrElse(key, { createNewValue() }) as D }

    /**
     * Empties the cache
     */
    fun flush() = this.map.clear()

}
