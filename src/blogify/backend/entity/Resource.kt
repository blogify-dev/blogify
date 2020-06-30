package blogify.backend.entity

import reflectr.models.Identified
import reflectr.entity.Entity
import blogify.backend.events.models.EventEmitter
import blogify.backend.events.models.EventSource
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.util.never

import java.util.*

@ExperimentalStdlibApi
abstract class Resource(override val uuid: UUID = UUID.randomUUID()) : Entity(uuid),
    EventSource,
    EventEmitter,
    Identified {

    @Suppress("PropertyName")
    open val __type = this::class.simpleName?.toLowerCase() ?: never

    /**
     * This function is run when the resource is created. Not to confuse with the constructor;
     * [Resource] subtypes can be constructed at any moment.
     *
     * @param request the [RequestContext] in which the creation originated
     *
     * @author Benjozork
     */
    @Suppress("RedundantSuspendModifier")
    open suspend fun onCreation(request: RequestContext) = Unit

}
