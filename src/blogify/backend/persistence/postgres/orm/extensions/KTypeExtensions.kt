package blogify.backend.persistence.postgres.orm.extensions

import blogify.backend.resources.models.Resource
import com.andreapivetta.kolor.red
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

@Suppress("UNCHECKED_CAST")
fun <TClass : Any> KType.klass(): KClass<out TClass> =
    this.classifier as? KClass<TClass> ?: error("cannot get class for a type projection".red())

fun KType.isResource() =
    this subtypeOf Resource::class

fun KType.isPrimitive() =
    (this subtypeOf Number::class ||
     this isType    String::class ||
     this isType    Char::class)

infix fun KType.subtypeOf(klass: KClass<*>) =
    this.isSubtypeOf(klass.createType(klass.typeParameters.map { KTypeProjection(null, null) }, nullable = this.isMarkedNullable))

infix fun KType.isType(klass: KClass<*>) =
    this.classifier == klass
