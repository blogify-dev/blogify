package blogify.backend.persistence.postgres.orm.extensions

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

infix fun KType.subtypeOf(klass: KClass<*>) =
    this.isSubtypeOf(klass.createType(klass.typeParameters.map { KTypeProjection(null, null) }))

infix fun KType.isType(klass: KClass<*>) =
    this.classifier == klass
