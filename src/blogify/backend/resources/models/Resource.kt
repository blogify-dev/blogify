package blogify.backend.resources.models

import com.fasterxml.jackson.annotation.ObjectIdGenerator
import com.fasterxml.jackson.annotation.ObjectIdResolver

import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.services.UserService
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.articles.CommentService
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.ktor.application.Application

import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.request.ApplicationRequest
import io.ktor.response.ApplicationResponse
import io.ktor.util.Attributes

import kotlinx.coroutines.runBlocking

import java.lang.IllegalStateException
import java.util.*

open class Resource(open val uuid: UUID = UUID.randomUUID()) {

    data class ResolverKey(val callContext: ApplicationCall, val id: UUID)

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

        override fun resolveId(id: ObjectIdGenerator.IdKey?): Any? {

            val uuid = id?.key as UUID

            fun genException(scope: Class<*>, ex: Exception)
                    = IllegalStateException("exception during resource (type: ${scope.simpleName}) resolve with UUID $uuid : ${ex.message}", ex)

            return runBlocking {
                // Necessary since we're interacting with Java cruft
                when (id.scope) {

                    Article::class.java -> {
                        try {
                            return@runBlocking ArticleService.get(id = uuid).get()
                        } catch (e: Exception) {
                            throw genException(id.scope, e)
                        }
                    }

                    User::class.java -> {
                        try {
                            return@runBlocking UserService.get(id = uuid).get()
                        } catch (e: Exception) {
                            throw genException(id.scope, e)
                        }
                    }

                    Comment::class.java -> {
                        try {
                            return@runBlocking CommentService.get(id = uuid).get()
                        } catch (e: Exception) {
                            throw genException(id.scope, e)
                        }
                    }

                    else -> {
                        error("can't find service for resource class ${id.scope.simpleName}")
                    }

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

}
