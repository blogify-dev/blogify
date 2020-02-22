package blogify.backend.util

open class MapCache<K : Any, V : Any> {

    private val map = mutableMapOf<K, V>()

    open operator fun get(key: K): V? = map[key]

    operator fun set(key: K, value: V) = map.put(key, value)

    private suspend fun getOrElse(key: K, createNewValue: suspend () -> V): V {
        val attempt = this[key]
        return if (attempt != null) attempt
        else {
            val newValue = createNewValue()
            this[key] = newValue
            return newValue
        }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <D : V> findOr(key: K, createNewValue: () -> D): Sr<D>
            =  WrapBlocking { this@MapCache.getOrElse(key, { createNewValue() }) as D }

    @Suppress("UNCHECKED_CAST")
    open suspend fun <D : V> findOrAsync(key: K, createNewValue: suspend () -> D): Sr<D>
            = Wrap { this.getOrElse(key, { createNewValue() }) as D }

    fun flush() = this.map.clear()

}
