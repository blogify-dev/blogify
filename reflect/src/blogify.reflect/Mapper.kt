package blogify.reflect

import blogify.reflect.annotations.Invisible
import blogify.reflect.annotations.check
import blogify.reflect.computed.models.Computed

import blogify.reflect.models.Mapped
import blogify.reflect.models.Identified
import blogify.reflect.models.PropMap

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

//private val logger = LoggerFactory.getLogger("blogify-datamap")

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
private fun <M : Mapped> KClass<M>.buildPropMap(unsafe: Boolean = false): PropMap {
    return PropMap(this.declaredMemberProperties
        .asSequence()
        .associateBy {
            it.name
        }
        .mapValues<String, KProperty1<*, *>, PropMap.PropertyHandle> { (name, self) ->
            if ((self.findAnnotation<Invisible>() != null || self.visibility != KVisibility.PUBLIC) && !unsafe) {
                PropMap.PropertyHandle.AccessDenied(name)
            } else {
                if (self.findAnnotation<Computed>() != null) {
                    if (!this.isSubclassOf(Identified::class))
                        error("@Computed property can only appear on classes extending Resource")

                    PropMap.PropertyHandle.Computed(
                        name,
                        self as KProperty1<Any, Any>
                    )
                } else {
                    if (self.returnType.findAnnotation<check>() != null) {
                        val regex = Regex(self.returnType.findAnnotation<check>()!!.pattern)

                        PropMap.PropertyHandle.Ok(
                            name,
                            regex,
                            self as KProperty1<Any, Any>
                        )
                    } else {
                        PropMap.PropertyHandle.Ok(
                            name,
                            null,
                            self as KProperty1<Any, Any>
                        )
                    }
                }
            }
        }/*.also { logger.debug("built propmap for class ${this.simpleName}".green()) }*/
    )
}

/**
 * A cache storing computed [property maps][PropMap] for various [classes][KClass], using the [class][KClass] itself as a key
 *
 * @author Benjozork
 */
private val propMapCache: MutableMap<KClass<*>, PropMap> = mutableMapOf()

/**
 * A cache storing computed unsage [property maps][PropMap] for various [classes][KClass], using the [class][KClass] itself as a key
 *
 * @author Benjozork
 */
private val unsafePropMapCache: MutableMap<KClass<*>, PropMap> = mutableMapOf()

/**
 * Fetches (or computes if the class is not in the cache) a [property map][PropMap] for the receiver [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> M.propMap(): PropMap {
    var cached: PropMap? = propMapCache[this::class]
    if (cached == null) {
        cached = this::class.buildPropMap()
        propMapCache[this::class] = cached
    }

    return cached
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
val <M : Mapped> KClass<M>.propMap: PropMap
    get() {
        var cached: PropMap? = propMapCache[this]
        if (cached == null) {
            cached = this.buildPropMap()
            propMapCache[this] = cached
        }

        return cached
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
val <M : Mapped> M.unsafePropMap: PropMap
    get() {
        var cached: PropMap? = unsafePropMapCache[this::class]
        if (cached == null) {
            cached = this::class.buildPropMap(unsafe = true)
            unsafePropMapCache[this::class] = cached
        }

        return cached
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
fun <M : Mapped> KClass<M>.unsafePropMap(): PropMap {
    var cached: PropMap? = unsafePropMapCache[this]
    if (cached == null) {
        cached = this.buildPropMap(unsafe = true)
        unsafePropMapCache[this] = cached
    }

    return cached
}
