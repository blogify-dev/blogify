package blogify.backend.resources.reflect

import blogify.backend.util.Sr
import reflect.cachedUnsafePropMap
import NotResouce
import blogify.backend.resources.models.Resource

import com.andreapivetta.kolor.red
import reflect.models.Mapped
import reflect.models.PropMap
import reflect.models.extensions.ok
import reflect.slice

import java.util.UUID

import kotlin.reflect.KClass

/**
 * Updates a [Resource] using a map of [`Ok` handles][PropMap.PropertyHandle.Ok] to new data values
 *
 * @receiver the [Resource] to update
 *
 * @param R       the class associated with [this]
 * @param rawData a map of [`Ok` handles][PropMap.PropertyHandle.Ok] to new data values
 *
 * @return an updated instance of [R] with all new data from [rawData], but the same unchanged data from [target]
 *
 * @author Benjozork
 */
suspend fun <R : Mapped> R.update (
    rawData: Map<PropMap.PropertyHandle.Ok, Any?>,
    fetcher: suspend (KClass<Resource>, UUID) -> Sr<Resource>
): Sr<R> {

    val targetPropMap = this.cachedUnsafePropMap() // Get unsafe handles too

    // Find parameters we are not changing
    val notUpdatedParameters = (targetPropMap.ok().values subtract rawData.keys)

    // Find parameters we are changing
    val updatedParameters = (targetPropMap.ok().values intersect rawData.keys)

    // Find the values of the unchanged params
    val unchangedValues = this
        .slice(notUpdatedParameters.map { it.name }.toSet(), unsafe = true)
        .filter { !it.key.startsWith('_') }
        .mapKeys { targetPropMap.ok()[it.key] ?: error("fatal: unknown propHandle slipped in !") }

    // Find the values of the changed params
    val changedValues = updatedParameters
        .associateWith { rawData[it] }

    return this::class.doInstantiate(unchangedValues + changedValues, fetcher)

}
