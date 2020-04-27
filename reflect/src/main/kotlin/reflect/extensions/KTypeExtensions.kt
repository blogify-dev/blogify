package reflect.extensions

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

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
