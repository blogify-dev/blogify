package blogify.backend.resources.models

import blogify.backend.appContext
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventEmitter
import blogify.backend.events.models.EventSource
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.reflect.models.Mapped

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.request.ApplicationRequest
import io.ktor.response.ApplicationResponse
import io.ktor.util.Attributes

import com.fasterxml.jackson.annotation.ObjectIdGenerator
import com.fasterxml.jackson.annotation.ObjectIdResolver
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking

import kotlin.reflect.KClass

import java.lang.IllegalStateException
import java.util.*

abstract class Resource(override val uuid: UUID = UUID.randomUUID()) : Mapped(), EventSource, EventEmitter, Identified {

    object ObjectResolver : ObjectIdResolver {

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

        val FakeRequestContext = RequestContext(appContext, GlobalScope, FakeApplicationCall, enableCaching = false)

        override fun resolveId(id: ObjectIdGenerator.IdKey?): Any? {

            val uuid = id?.key as UUID

            fun genException(scope: Class<*>, ex: Exception)
                    = IllegalStateException("exception during resource (type: ${scope.simpleName}) resolve with UUID $uuid : ${ex.message}", ex)

            return runBlocking {

                try {
                    @Suppress("UNCHECKED_CAST")
                    FakeRequestContext.repository(id.scope.kotlin as KClass<Resource>).get(id = uuid).get()
                } catch (e: Exception) {
                    throw genException(id.scope, e)
                }

            }

        }

        override fun newForDeserialization(context: Any?): ObjectIdResolver {
           return this
        }

        override fun bindItem(id: ObjectIdGenerator.IdKey?, pojo: Any?) {
            return
        }

        override fun canUseFor(resolverType: ObjectIdResolver?): Boolean {
            return resolverType!!::class == this::class
        }

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
