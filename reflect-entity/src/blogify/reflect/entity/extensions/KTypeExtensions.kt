package blogify.reflect.entity.extensions

import blogify.common.util.never
import blogify.reflect.entity.Entity
import blogify.reflect.extensions.isCollection
import blogify.reflect.extensions.subTypeOf

import kotlin.reflect.KType

/**
 * Returns whether a [KType] is a subtype of [blogify.reflect.entity.Entity]
 *
 * @author Benjozork
 */
fun KType.isEntity(): Boolean =
    this subTypeOf Entity::class || (this.isCollection() && this.arguments.first().type?.isEntity() ?: never)
