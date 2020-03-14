package blogify.backend.resources.reflect

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.util.Sr
import blogify.backend.util.Wrap
import blogify.backend.util.toUUID

import java.util.UUID

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

import com.andreapivetta.kolor.red

//suspend inline fun <reified TMapped : Resource> KClass<TMapped>.from(dto: Dto, requestContext: RequestContext)
//        = this.doInstantiate(dto) { requestContext.repository<TMapped>().get(requestContext, it) }

private val noExternalFetcherMessage =
    "fatal: tried to instantiate an object that with references to resources but no external fetcher was provided".red()

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
    params:          Map<PropMap.PropertyHandle.Ok, Any?>,
    externalFetcher: suspend (KClass<Resource>, UUID) -> Sr<Any> = { _, _ -> error(noExternalFetcherMessage) }
): Sr<TMapped> {
    // We use unsafe because we have to give the @Invisible values too
    val propMap = this.cachedUnsafePropMap()

    // We use the first ctor to instantiate the object
    val targetCtor = this.constructors.first()

    // We make this a function so that Wrap {} catches any error in it
    suspend fun makeParamMap(): Map<KParameter, Any?> {
        return (propMap.ok().values intersect params.keys)
            // For now, associate each propHandle to the constructor param with the same name
            .associateWith { targetCtor.parameters.firstOrNull { p -> p.name == it.name } ?: error("fatal: impossible state") }
            // Then map it to the given value in params
            .map { it.value to params[it.key] }
            .map { (k ,v) -> // Do some known obvious conversions
                when {
                    k.type.isSubtypeOf(Resource::class.createType()) -> { // KType of property is subtype of Resource
                        @Suppress("UNCHECKED_CAST")
                        val keyResourceType = k.type.classifier as KClass<Resource>
                        val valueUUID = (v as String).toUUID()

                        k to externalFetcher(keyResourceType, valueUUID).get()
                    }
                    k.type.isSubtypeOf(UUID::class.createType()) -> { // KType of property is subtype of UUID
                        k to (v as String).toUUID()
                    }
                    else -> k to v
                }
            }.toMap()
    }

    return Wrap { targetCtor.callBy(makeParamMap()) }
}
