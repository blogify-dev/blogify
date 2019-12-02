package blogify.backend.resources.computed

import blogify.backend.resources.models.Resource

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class CompoundCacheKey(val klass: KClass<*>, val property: KProperty<*>)
val compoundCache = ConcurrentHashMap<CompoundCacheKey, Map<UUID, Any>>()

class CompoundCachedComputedPropertyDelegate<A : Any> (
    val initializer: () -> Map<UUID, A>
) {

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Resource, property: KProperty<*>): A {
        val key = CompoundCacheKey(thisRef::class, property)
        var cacheValue = compoundCache[key]?.toMutableMap()

        if (cacheValue == null) {
            cacheValue = initializer().toMutableMap()
            compoundCache[key] = cacheValue
        }

        var finalValue = cacheValue[thisRef.uuid]
        cacheValue.remove(thisRef.uuid)
        compoundCache[key] = cacheValue

        if (finalValue == null) {
            cacheValue = initializer().toMutableMap()
            compoundCache[key] = cacheValue
            finalValue = cacheValue[thisRef.uuid]
        }

        return finalValue as A
    }

}

/**
 * `compound` is a property delegate that allows running a costly operation only once
 */
fun <A : Any> compound(initializer: suspend () -> Map<UUID, A>)
        = CompoundCachedComputedPropertyDelegate { runBlocking(context = Dispatchers.IO, block = { initializer() }) };
