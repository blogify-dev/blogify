package blogify.reflect.extensions

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

/**
 * Returns a [type][KType]'s [class][KClass] or `null` if the type is not representable
 *
 * @author Benjozork
 */
fun KType.klass() = this.classifier as? KClass<*>

/**
 * Returns a [type][KType]'s [class][KClass] extending [TClass] or `null` if the type is not representable or if the
 * classifier is not a subtype of [TClass]
 *
 * @author Benjozork
 */
fun <TClass : Any> KType.safeKlass() = this.classifier as? KClass<TClass>

/**
 * Returns whether a [KType] is a subtype of a class
 *
 * @author Benjozork
 */
infix fun KType.subTypeOf(klass: KClass<*>): Boolean {
    val klassTypeArguments = klass.typeParameters.map { KTypeProjection.STAR }
    val klassType = klass.createType(klassTypeArguments, true)

    return this.isSubtypeOf(klassType)
}

/**
 * Returns whether a [KType] is a subtype of a primitive class
 *
 * @author Benjozork
 */
fun KType.isPrimitive() =
    this subTypeOf Number::class ||
    this subTypeOf String::class ||
    this subTypeOf Boolean::class ||
    this subTypeOf Char::class

/**
 * Returns whether a [KType] is a subtype of a collection class
 *
 * @author Benjozork
 */
fun KType.isCollection() =
    this subTypeOf Collection::class
