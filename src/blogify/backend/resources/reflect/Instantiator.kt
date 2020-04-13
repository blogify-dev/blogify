package blogify.backend.resources.reflect

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.never
import blogify.backend.util.toUUID

import java.util.UUID

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

import com.andreapivetta.kolor.red

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import com.github.kittinunf.result.coroutines.mapError

import java.lang.IllegalStateException

//suspend inline fun <reified TMapped : Resource> KClass<TMapped>.from(dto: Dto, requestContext: RequestContext)
//        = this.doInstantiate(dto) { requestContext.repository<TMapped>().get(requestContext, it) }

private class MissingArgumentsException(vararg val parameters: KParameter)
    : IllegalArgumentException("missing value(s) for parameter(s) ${parameters.joinToString(prefix = "[", postfix = "]") { it.name.toString() }}")

private val noExternalFetcherMessage =
    "fatal: tried to instantiate an object with references to resources but no external fetcher was provided".red()

/**
 * This is fine, it's thread safe
 */
private val objectMapper = jacksonObjectMapper()

/**
 * Instantiates the class in receiver position using a [Map] of [property handles][PropMap.PropertyHandle] and
 * associated values.
 *
 * @receiver the [KClass] we wish to instantiate
 *
 * @param params          the map of property handles to values we are going to be using to instantiate the object.
 *                        All primary constructor properties must be present or else the returned [Sr] will be a failure.
 * @param externalFetcher a function that is used to fetch other [resources][Resource] requires by the instantiated objects.
 *                        Takes the type of the property and an [UUID].
 *
 * @return the instantiated object
 *
 * @author Benjozork
 */
suspend fun <TMapped : Mapped> KClass<out TMapped>.doInstantiate (
    params:             Map<PropMap.PropertyHandle.Ok, Any?>,
    externalFetcher:    suspend (KClass<Resource>, UUID) -> Sr<Any> = { _, _ -> error(noExternalFetcherMessage) },
    externallyProvided: Set<PropMap.PropertyHandle.Ok> = setOf()
): Sr<TMapped> {

    // We use unsafe because we have to give the @Invisible values too
    val propMap = this.cachedUnsafePropMap()

    // We use the first ctor to instantiate the object
    val targetCtor = this.constructors.first()

    // We make this a function so that Wrap {} catches any error in it
    suspend fun makeParamMap(): Map<KParameter, Any?> {
        return ((propMap.ok().values intersect params.keys))
            // For now, associate each propHandle to the constructor param with the same name
            .associateWith { targetCtor.parameters.firstOrNull { p -> p.name == it.name } ?: never }
            // Then map it to the given value in params
            .map { (it.value to it.key) to params[it.key] }
            .map { (parameterAndHandle ,value) -> // Do some known obvious conversions
                val (parameter, handle) = parameterAndHandle

                when {
                    handle in externallyProvided -> { // Do not deserialize, it's provided !
                        parameter to value
                    }
                    parameter.type.isSubtypeOf(Resource::class.createType()) -> { // KType of property is subtype of Resource
                        @Suppress("UNCHECKED_CAST")
                        val keyResourceType = parameter.type.classifier as KClass<Resource>
                        val valueUUID = (value as String).toUUID()

                        parameter to externalFetcher(keyResourceType, valueUUID).get()
                    }
                    parameter.type.isSubtypeOf(UUID::class.createType()) -> { // KType of property is subtype of UUID
                        parameter to (value as String).toUUID()
                    }
                    else -> { // We don't know what it is, we need to extract a JavaType and make Jackson deserialize it
                        val baseTypeClass = (parameter.type.classifier as? KClass<*>)?.java
                            ?: error("fatal: found non-class base type when extracting JavaType of parameter '${parameter.name}' of class '${this.simpleName}'".red())

                        val typeParameters = parameter.type.arguments.map {
                            (it.type?.classifier as? KClass<*>)?.java
                                ?: error("fatal: found non-class type parameter when extracting JavaType of parameter '${parameter.name}' of class '${this.simpleName}'".red())
                        }.toTypedArray()

                        val type = objectMapper.typeFactory.constructParametricType(baseTypeClass, *typeParameters)

                        parameter to objectMapper.readValue(objectMapper.writeValueAsString(value).toByteArray(), type)
                    }
                }
            }.toMap().also { map ->
                val missingParams = targetCtor.parameters subtract map.keys
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
