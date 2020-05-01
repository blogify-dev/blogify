package blogify.backend.resources.reflect

import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.search.models.Template
import blogify.backend.util.*

import java.util.UUID

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import io.ktor.http.ContentType

import com.github.kittinunf.result.coroutines.mapError

import java.lang.IllegalStateException

import com.andreapivetta.kolor.red

import blogify.reflect.cachedUnsafePropMap
import blogify.reflect.extensions.isPrimitive
import blogify.reflect.extensions.subTypeOf
import blogify.reflect.models.Mapped
import blogify.reflect.models.PropMap
import blogify.reflect.models.extensions.ok

//suspend inline fun <reified TMapped : Resource> KClass<TMapped>.from(dto: blogify.reflect.Dto, requestContext: RequestContext)
//        = this.doInstantiate(dto) { requestContext.repository<TMapped>().get(requestContext, it) }

private class MissingArgumentsException(vararg val parameters: KParameter)
    : IllegalArgumentException("missing value(s) for parameter(s) ${parameters.joinToString(prefix = "[", postfix = "]") { it.name.toString() }}")

private val noExternalFetcherMessage =
    "fatal: tried to instantiate an object with references to resources but no external fetcher was provided".red()

/**
 * This is fine, it's thread safe
 */
private val objectMapper = jacksonObjectMapper().apply {
    val instantiatorModule = SimpleModule()

    instantiatorModule.addSerializer(Resource.ResourceIdSerializer)
    instantiatorModule.addSerializer(Template.Field.Serializer)
    instantiatorModule.addSerializer(ContentTypeSerializer)
    instantiatorModule.addSerializer(InstantSerializer)

    instantiatorModule.addDeserializer(ContentType::class.java, ContentTypeDeserializer)

    registerModule(instantiatorModule)
}

/**
 * Instantiates the class in receiver position using a [Map] of [property handles][PropMap.PropertyHandle] and
 * associated values.
 *
 * @receiver the [KClass] we wish to instantiate
 *
 * @param params          the map of property handles to values we are going to be using to instantiate the object.
 *                        All non-optional primary constructor properties must be present or else the returned [Sr] will be a failure.
 * @param externalFetcher a function that is used to fetch other [resources][Resource] requires by the instantiated objects.
 *                        Takes the type of the property and an [UUID].
 *
 * @return the instantiated object
 *
 * @author Benjozork
 */
@Suppress("UNCHECKED_CAST")
suspend fun <TMapped : Mapped> KClass<out TMapped>.doInstantiate (
    params:             Map<PropMap.PropertyHandle.Ok, Any?>,
    externalFetcher: suspend (KClass<Resource>, UUID) -> Sr<Any> = { _, _ -> error(noExternalFetcherMessage) },
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
            .associateWith { targetCtor.parameters.firstOrNull { p -> p.name == it.name } }
            .withoutNullValues() // Drop properties not in our ctor
            // Then map it to the given value in params
            .map { (it.value to it.key) to params[it.key] }
            .map { (parameterAndHandle, value) -> // Do some known obvious conversions
                val (parameter, handle) = parameterAndHandle

                if (value == null) {
                    if (parameter.type.isMarkedNullable)
                        return@map parameter to null
                    else error("fatal: null value provided for non-nullable type of parameter ${parameter.name}")
                }

                when {
                    handle in externallyProvided -> { // Do not deserialize, it's provided !
                        parameter to value
                    }
                    parameter.type subTypeOf Resource::class -> { // KType of property is subtype of Resource
                        @Suppress("UNCHECKED_CAST")
                        val keyResourceType = parameter.type.classifier as KClass<Resource>

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
                    parameter.type subTypeOf StaticFile::class -> { // Special case for SRH, since it's a sealed class
                        val valueString = objectMapper.writeValueAsString(value)
                        val valueMap = objectMapper.readValue<Map<String, Any?>>(valueString)

                        parameter to when {
                            valueMap.containsKey("metadata") -> {
                                objectMapper.readValue<StaticFile.Ok.Image>(valueString)
                            }
                            valueMap.containsKey("fileId") -> {
                                objectMapper.readValue<StaticFile.Ok>(valueString)
                            }
                            valueMap.containsKey("contentType") -> {
                                objectMapper.readValue<StaticFile.None>(valueString)
                            }
                            else -> never
                        }
                    }
                    parameter.type.isPrimitive() -> { // KType is primitive
                        parameter to value
                    }
                    else -> { // It's some other type, so we need to extract a JavaType from the parameter and make Jackson deserialize it
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
