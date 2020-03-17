package blogify.backend.resources.reflect

import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.toUUID
import com.andreapivetta.kolor.red
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import java.util.UUID

import org.slf4j.LoggerFactory

import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf

private val logger = LoggerFactory.getLogger("blogify-datamap-updater")

/**
 * Updates a [Resource] using a map of [`Ok` handles][PropMap.PropertyHandle.Ok] to new data values
 *
 * @param R       the class associated with [target]
 * @param target  the [Resource] to update
 * @param rawData a map of [`Ok` handles][PropMap.PropertyHandle.Ok] to new data values
 *s
 * @return an updated instance of [R] with all new data from [rawData], but the same unchanged data from [target]
 *
 * @author Benjozork
 */
suspend fun <R: Resource> update(target: R, requestContext: RequestContext, rawData: Map<PropMap.PropertyHandle.Ok, Any?>): Sr<R> {

    val ost = jacksonObjectMapper()

    val targetPropMap = target.cachedUnsafePropMap() // Get unsafe handles too
    val targetCopyFunction = target::class.functions.first { it.name == "copy" }

    // Make paramMap for unchanged properties

    val notUpdatedParameterMap = (targetPropMap.ok().values subtract rawData.keys)
        .associateWith { targetCopyFunction.parameters.first { p -> p.name == it.name } }
        .map { it.value to it.key.property.get(target) }

    // Make paramMap for changed properties

    val updatedParameterMap = (targetPropMap.ok().values intersect rawData.keys)
        .associateWith { targetCopyFunction.parameters.first { p -> p.name == it.name } }
        .map { it.value to rawData[it.key] }
        .map { (k ,v) -> // Do some known obvious conversions
            when {
                k.type.isSubtypeOf(Resource::class.createType()) -> { // KType of property is subtype of Resource
                    @Suppress("UNCHECKED_CAST")
                    val keyResourceType = k.type.classifier as KClass<Resource>
                    val valueUUID = (v as String).toUUID()
                    // I don't know if this works or not
                    k to requestContext.repository(keyResourceType).get(id = valueUUID).get()
                }
                k.type.isSubtypeOf(UUID::class.createType()) -> { // KType of property is subtype of UUID
                    k to (v as String).toUUID()
                }
                else -> { // We don't know what it is, we need to extract a JavaType and make Jackson deserialize it
                    val baseTypeClass = (k.type.classifier as? KClass<*>)?.java
                        ?: error("fatal: found non-class base type when extracting JavaType of parameter '${k.name}' of class '${target::class.simpleName}'".red())

                    val typeParameters = k.type.arguments.map {
                        (it.type?.classifier as? KClass<*>)?.java
                            ?: error("fatal: found non-class type parameter when extracting JavaType of parameter '${k.name}' of class '${target::class.simpleName}'".red())
                    }.toTypedArray()

                    val type = ost.typeFactory.constructParametricType(baseTypeClass, *typeParameters)

                    k to ost.readValue(ost.writeValueAsString(v).toByteArray(), type)
                }
            }
        }

    val completeData =
        (notUpdatedParameterMap + updatedParameterMap + (targetCopyFunction.parameters.first() to target)).toMap()
//    You sir, can fuck off. You're so unimportant that nobody even wants to use you
//    logger.trace("attempting with paramMap: ${completeData.map { "${it.key.name}: ${it.value!!::class.simpleName}" }}".yellow())
//    logger.trace("function wants paramMap: ${targetCopyFunction.parameters.map { "${it.name}: ${it.type.classifier}" } }".yellow())

    return Wrap {
        @Suppress("UNCHECKED_CAST")
        targetCopyFunction.callBy (
            completeData
        ) as R
    }

}
