package blogify.reflect.entity.typing

import blogify.common.util.never
import blogify.reflect.extensions.isCollection
import blogify.reflect.extensions.klass
import blogify.reflect.extensions.subTypeOf
import blogify.reflect.entity.extensions.isEntity

import kotlin.reflect.KProperty

/**
 * Resolves the [Kind] of the receiver property. Should not be used directly, as nothing is cached.
 *
 * @author Benjozork
 */
internal val KProperty<*>.kind: Kind get() {
    if (returnType.isEntity()) {
        return Kind(Kind.Type.Entity, returnType.isCollection())
    }

    val isCollection = returnType.isCollection()

    val typeToCheckAgainst = if (isCollection) returnType.arguments.first().type ?: never else returnType

    return when {
        typeToCheckAgainst subTypeOf String::class ->
            Kind(Kind.Type.String, isCollection)
        typeToCheckAgainst subTypeOf Int::class ||
        typeToCheckAgainst subTypeOf Long::class ||
        typeToCheckAgainst subTypeOf Float::class ||
        typeToCheckAgainst subTypeOf Double::class ->
            Kind(Kind.Type.Number, isCollection)
        else -> error("type ${typeToCheckAgainst.klass()?.simpleName ?: "<!!!>"} not supported as entity property kind")
    }

}
