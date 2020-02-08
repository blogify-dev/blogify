package blogify.backend.util

class MapCache<K, V> {

    private val map = mutableMapOf<K, V>()

    operator fun get(key: K): V? = map[key]

    operator fun set(key: K, value: V) = map.put(key, value)

    fun getOrElse(key: K, newValue: V): V {
        val attempt = this[key]
        return if (attempt != null) attempt
        else {
            this[key] = newValue
            newValue
        }
    }

    fun getOrElse(key: K, createNewValue: () -> V): V {
        val attempt = this[key]
        return if (attempt != null) attempt
        else {
            val newValue = createNewValue()
            this[key] = newValue
            return newValue
        }
    }

    fun flush() = this.map.clear()

}
