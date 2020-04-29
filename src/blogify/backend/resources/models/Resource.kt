package blogify.backend.resources.models

import blogify.reflect.models.Identified
import blogify.backend.appContext
import blogify.backend.events.models.EventEmitter
import blogify.backend.events.models.EventSource
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.reflect.models.Mapped

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.request.ApplicationRequest
import io.ktor.response.ApplicationResponse
import io.ktor.util.Attributes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import kotlinx.coroutines.GlobalScope

import java.util.*

abstract class Resource(override val uuid: UUID = UUID.randomUUID()) : Mapped(), EventSource, EventEmitter, Identified {

    object ObjectResolver {

        object FakeApplicationCall : ApplicationCall {

            override val application: Application
                get() = TODO("cannot be accessed in a fake ApplicationCall")
            override val attributes: Attributes
                get() = TODO("cannot be accessed in a fake ApplicationCall")
            override val parameters: Parameters
                get() = TODO("cannot be accessed in a fake ApplicationCall")
            override val request: ApplicationRequest
                get() = TODO("cannot be accessed in a fake ApplicationCall")
            override val response: ApplicationResponse
                get() = TODO("cannot be accessed in a fake ApplicationCall")

        }

        @Deprecated("Please remove instances of this")
        val FakeRequestContext = RequestContext(appContext, GlobalScope, FakeApplicationCall, enableCaching = false)

    }

    /**
     * Used to serialize [resources][Resource] by only printing their [uuid][Resource.uuid].
     */
    object ResourceIdSerializer : StdSerializer<Resource>(Resource::class.java) {

        override fun serialize(value: Resource, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.uuid.toString())
        }

    }

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

infix fun <T : Resource> T.eqr(other: T) = this.uuid == other.uuid
