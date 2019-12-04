package blogify.backend.resources.reflect

import blogify.backend.annotations.check
import blogify.backend.annotations.NoSlice
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap

import com.andreapivetta.kolor.green

import org.slf4j.LoggerFactory

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberExtensionProperties
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.getExtensionDelegate
import kotlin.reflect.full.memberExtensionProperties
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
fun <M : Mapped> KClass<M>.buildPropMap(): PropMap {
    return PropMap(this.declaredMemberProperties
        .asSequence()
        .associateBy {
            it.name
        }.mapValues<String, KProperty1<*, *>, PropMap.PropertyHandle> { (name, self) ->
            if (self.findAnnotation<NoSlice>() != null) {
                PropMap.PropertyHandle.AccessDenied(name)
            } else {
                if (self.findAnnotation<Computed>() != null) {
                    PropMap.PropertyHandle.Computed(name, self as KProperty1<Any, Any>)
                } else {
                    if (self.returnType.findAnnotation<check>() != null) {
                        val regex = Regex(self.returnType.findAnnotation<check>()!!.pattern)
                        PropMap.PropertyHandle.Ok(name, regex, self as KProperty1<Any, Any>)
                    } else {
                        PropMap.PropertyHandle.Ok(name, null, self as KProperty1<Any, Any>)
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
private val propMapCache: MutableMap<KClass<*>, PropMap> = mutableMapOf()

/**
 * Fetches (or computes if the class is not in the cache) a [property map][PropMap] for the reciever [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> M.cachedPropMap(): PropMap {
    var cached: PropMap? = propMapCache[this::class]
    if (cached == null) {
        cached = this::class.buildPropMap()
        propMapCache[this::class] = cached
    }

    return cached
}

/**
 * Fetches (or computes if the class is not in the cache) a [property map][PropMap] for the reciever [KClass]
 *
 * @receiver the [class][KClass] for which the [PropMap] should be obtained
 *
 * @return the obtained [PropMap]
 *
 * @author Benjozork
 */
fun <M : Mapped> KClass<M>.cachedPropMap(): PropMap {
    var cached: PropMap? = propMapCache[this]
    if (cached == null) {
        cached = this.buildPropMap()
        propMapCache[this] = cached
    }

    return cached
}
