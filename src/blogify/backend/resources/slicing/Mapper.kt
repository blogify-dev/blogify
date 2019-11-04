package blogify.backend.resources.slicing

import blogify.backend.annotations.check
import blogify.backend.annotations.noslice
import blogify.backend.resources.slicing.models.Mapped

import com.andreapivetta.kolor.green

import org.slf4j.LoggerFactory

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

private val logger = LoggerFactory.getLogger("blogify-datamap")

/**
 * Represents a handle on a [KProperty1]. Can be either [Ok] or [AccessDenied] to represent the state of the handle.
 *
 * @property name the canonical name of the property
 *
 * @author Benjozork
 */
sealed class PropertyHandle(val name: String) {
    /**
     * Represents a valid handle, which points to a [KProperty1]
     *
     * @param name the canonical name of the property
     *
     * @property regexCheck a regular expression that the value of the property is checked against by [verify] if the property is of type [String]
     * @property property   the property that is pointed to
     *
     * @author Benjozork
     */
    class Ok(name: String, val regexCheck: Regex?, val property: KProperty1<Any, Any>) : PropertyHandle(name)

    /**
     * Represents a handle that points to a property that cannot be accessed due to security policy reasons. This incident will be reported.
     *
     * @param name the canonical name of the property
     *
     * @author Benjozork
     */
    class AccessDenied(name: String) : PropertyHandle(name)
}

/**
 * Syntactic sugar for Map<[String], [PropertyHandle]>
 *
 * @author Benjozork
 */
typealias PropMap = Map<String, PropertyHandle>

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
    return this.declaredMemberProperties
        .asSequence()
        .associateBy {
            it.name
        }.mapValues { (name, self) ->
            if (self.findAnnotation<noslice>() != null) {
                PropertyHandle.AccessDenied(name)
            } else {
                if (self.returnType.findAnnotation<check>() != null) {
                    val regex = Regex(self.returnType.findAnnotation<check>()!!.pattern)
                    PropertyHandle.Ok(name, regex, self as KProperty1<Any, Any>)
                } else {
                    PropertyHandle.Ok(name, null, self as KProperty1<Any, Any>)
                }
            }
        }.also { logger.debug("built propmap for class ${this.simpleName}".green()) }
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

fun <M : Mapped> KClass<M>.cachedPropMap(): PropMap {
    var cached: PropMap? = propMapCache[this]
    if (cached == null) {
        cached = this.buildPropMap()
        propMapCache[this] = cached
    }

    return cached
}
