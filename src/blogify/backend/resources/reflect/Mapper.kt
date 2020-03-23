package blogify.backend.resources.reflect

import blogify.backend.annotations.check
import blogify.backend.annotations.Invisible
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap

import com.andreapivetta.kolor.green

import org.slf4j.LoggerFactory

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

private val logger = LoggerFactory.getLogger("blogify-datamap")

/**
 * Builds a [property map][PropMap] on the receiver [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be built
 *
 * @return the generated [PropMap]
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
private fun <M : Mapped> KClass<M>.buildPropMap(unsafe: Boolean = false): PropMap<M> {
    return PropMap(this, this.memberProperties
        .asSequence()
        .associateBy {
            it.name
        }.mapValues<String, KProperty1<*, *>, PropMap.PropertyHandle<M>> { (name, self) ->
            if (self.findAnnotation<Invisible>() != null && !unsafe) {
                PropMap.PropertyHandle.AccessDenied(this, name)
            } else {
                if (self.findAnnotation<Computed>() != null) {
                    PropMap.PropertyHandle.Computed(this, name, self as KProperty1<Any, Any>)
                } else {
                    if (self.returnType.findAnnotation<check>() != null) {
                        val regex = Regex(self.returnType.findAnnotation<check>()!!.pattern)
                        PropMap.PropertyHandle.Ok(this, name, regex, self as KProperty1<Any, Any>)
                    } else {
                        PropMap.PropertyHandle.Ok(this ,name, null, self as KProperty1<Any, Any>)
                    }
                }
            }
        }.also { logger.debug("built propmap for class ${this.simpleName}".green()) })
}

/**
 * A cache storing computed [property maps][PropMap] for various [classes][KClass], using the [class][KClass] itself as a key
 *
 * @author Benjozork
 */
private val propMapCache: MutableMap<KClass<*>, PropMap<*>> = mutableMapOf()

/**
 * A cache storing computed unsage [property maps][PropMap] for various [classes][KClass], using the [class][KClass] itself as a key
 *
 * @author Benjozork
 */
private val unsafePropMapCache: MutableMap<KClass<*>, PropMap<*>> = mutableMapOf()

/**
 * Fetches (or computes if the class is not in the cache) a [property map][PropMap] for the receiver [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> M.cachedPropMap(): PropMap<M> {
    var cached: PropMap<*>? = propMapCache[this::class]
    if (cached == null) {
        cached = this::class.buildPropMap()
        propMapCache[this::class] = cached
    }

    @Suppress("UNCHECKED_CAST")
    return cached as PropMap<M>
}

/**
 * Fetches (or computes if the class is not in the cache) a [property map][PropMap] for the receiver [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> KClass<M>.cachedPropMap(): PropMap<M> {
    var cached: PropMap<*>? = propMapCache[this]
    if (cached == null) {
        cached = this.buildPropMap()
        propMapCache[this] = cached
    }

    @Suppress("UNCHECKED_CAST")
    return cached as PropMap<M>
}

/**
 * Fetches (or computes if the class is not in the cache) an unsafe [property map][PropMap] for the receiver [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> M.cachedUnsafePropMap(): PropMap<M> {
    var cached: PropMap<*>? = unsafePropMapCache[this::class]
    if (cached == null) {
        cached = this::class.buildPropMap(unsafe = true)
        unsafePropMapCache[this::class] = cached
    }

    @Suppress("UNCHECKED_CAST")
    return cached as PropMap<M>
}

/**
 * Fetches (or computes if the class is not in the cache) an unsafe [property map][PropMap] for the receiver [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> KClass<M>.cachedUnsafePropMap(): PropMap<M> {
    var cached: PropMap<*>? = unsafePropMapCache[this]
    if (cached == null) {
        cached = this.buildPropMap(unsafe = true)
        unsafePropMapCache[this] = cached
    }

    @Suppress("UNCHECKED_CAST")
    return cached as PropMap<M>
}
