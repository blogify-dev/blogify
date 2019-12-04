package blogify.backend.resources.reflect.models

import kotlin.reflect.KProperty1

/**
 * Represents the map of different properties that can be obtained, changed or set on a [Mapped] object, depending
 * on the annotation the property possesses.
 *
 * @property map the map of [property names][String] to [property handles][PropertyHandle]
 *
 * @author Benjozork, hamza1311
 */
class PropMap(val map: Map<String, PropertyHandle>): Iterable<Map.Entry<String, PropMap.PropertyHandle>> {

    override fun iterator() = this.map.iterator()

    operator fun get(name: String) = this.map[name]

    /**
     * Represents a handle on a [KProperty1]. Can be either [Ok] or [AccessDenied] to represent the state of the handle.
     *
     * @property name the canonical name of the property
     *
     * @author Benjozork
     */
    sealed class PropertyHandle(val name: String) {

        interface Valid { val property: KProperty1<Any, Any> }

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
        class Ok(name: String, val regexCheck: Regex?, override val property: KProperty1<Any, Any>): PropertyHandle(name), Valid

        /**
         * Represents a valid computed handle, which points to a [KProperty1]
         *
         * @param name the canonical name of the property
         *
         * @property property the property that is pointed to
         *
         * @author Benjozork
         */
        class Computed(name: String, override val property: KProperty1<Any, Any>): PropertyHandle(name), Valid

        /**
         * Represents a handle that points to a property that cannot be accessed due to security policy reasons. This incident will be reported.
         *
         * @param name the canonical name of the property
         *
         * @author Benjozork
         */
        class AccessDenied(name: String): PropertyHandle(name)
    }

}
