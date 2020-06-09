package blogify.backend.resources.reflect

import blogify.backend.appContext
import blogify.reflect.entity.Entity
import blogify.backend.util.*
import blogify.reflect.MappedData
import blogify.reflect.extensions.*
import blogify.reflect.unsafePropMap
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok

import java.util.UUID
import java.lang.IllegalStateException

import kotlin.reflect.*
import kotlin.reflect.full.cast

import com.github.kittinunf.result.coroutines.mapError

import com.andreapivetta.kolor.red

class MissingArgumentsException(vararg val parameters: KParameter)
    : IllegalArgumentException("missing value(s) for parameter(s) ${parameters.joinToString(prefix = "[", postfix = "]") { it.name.toString() }}")

private val noExternalFetcherMessage =
    "fatal: tried to instantiate an object with references to resources but no external fetcher was provided".red()

/**
 * Instantiates the class in receiver position using a [Map] of [property handles][PropMap.PropertyHandle] and
 * associated values.
 *
 * @receiver the [KClass] we wish to instantiate
 *
 * @param data            the [data][MappedData] we are going to be using to instantiate the object.
 *                        All non-optional primary constructor properties must be present or else the returned [Sr] will be a failure.
 * @param externalFetcher a function that is used to fetch other [resources][Entity] requires by the instantiated objects.
 *                        Takes the type of the property and an [UUID].
 *
 * @return the instantiated object
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
suspend fun <TMapped : Mapped> KClass<out TMapped>.construct (
    data:               MappedData,
    externalFetcher:    suspend (KClass<Entity>, UUID) -> Sr<Entity> = { _, _ -> error(noExternalFetcherMessage) },
    externallyProvided: Set<PropMap.PropertyHandle.Ok> = setOf()
): Sr<TMapped> {

    // We use unsafe because we have to give the @Invisible values too
    val propMap = this.unsafePropMap

    // We use the first ctor to instantiate the object
    val targetCtor = this.constructors.first()

    // We make this a function so that Wrap {} catches any error in it
    suspend fun makeParamMap(): Map<KParameter, Any?> {
        return ((propMap.ok.values intersect data.keys))
            // For now, associate each propHandle to the constructor param with the same name
            .associateWith { targetCtor.parameters.firstOrNull { p -> p.name == it.name } }
            .withoutNullValues() // Drop properties not in our ctor
            // Then map it to the given value in params
            .map { (it.value to it.key) to data[it.key] }
            .map { (parameterAndHandle, value) -> // Do some known obvious conversions
                val (parameter, handle) = parameterAndHandle

                if (value == null) {
                    if (parameter.type.isMarkedNullable)
                        return@map parameter to null
                    else error("fatal: null value provided for non-nullable type of parameter ${parameter.name}")
                }

                val objectMapper = appContext.objectMapper

                when {
                    handle in externallyProvided -> { // Do not deserialize, it's provided !
                        parameter to value
                    }
                    parameter.type subTypeOf Entity::class -> { // KType of property is subtype of Resource
                        if (value is Entity)
                            return@map parameter to value

                        val keyResourceType = parameter.type.safeKlass<Entity>() ?: never

                        val valueUUID = when (value) {
                            is String -> value.toUUID()
                            is UUID -> value
                            else -> never
                        }

                        parameter to externalFetcher(keyResourceType, valueUUID).get()
                    }
                    parameter.type subTypeOf UUID::class -> { // KType of property is subtype of UUID
                        parameter to when (value) {
                            is String -> value.toUUID()
                            is UUID   -> value
                            else -> never
                        }
                    }
                    parameter.type.isPrimitive() ||
                    !parameter.type.isCollection() &&
                            (letCatchingOrNull { parameter.type.klass()!!.cast(value) } != null) -> { // Value is primitive or already <: KType
                        parameter to value
                    }
                    else -> { // It's some other type that isn't deserialized yet, so we need to extract a JavaType from the parameter and make Jackson deserialize it
                        val baseTypeClass = parameter.type.klass()?.java
                            ?: error("found non-class base type when extracting JavaType of parameter '${parameter.name}' of class '${this.simpleName}'".red())

                        val typeParameters = parameter.type.arguments.map {
                            it.type?.klass()?.java
                                ?: error("found non-class type parameter when extracting JavaType of parameter '${parameter.name}' of class '${this.simpleName}'".red())
                        }.toTypedArray()

                        val type = objectMapper.typeFactory.constructParametricType(baseTypeClass, *typeParameters)

                        parameter to objectMapper.readValue(objectMapper.writeValueAsString(value).toByteArray(), type)
                    }
                }
            }.toMap().also { map ->
                val missingParams = targetCtor.parameters.filterNot { it.isOptional } subtract map.keys

                if (missingParams.isNotEmpty())
                    throw MissingArgumentsException(*missingParams.toTypedArray())
            }
    }

    return Wrap { targetCtor.callBy(makeParamMap()) }
        .mapError { error ->
            if (error is MissingArgumentsException) error
            else IllegalStateException("exception while instantiating class ${this.simpleName}", error.cause ?: error)
        }
}
