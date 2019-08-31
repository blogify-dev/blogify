package blgoify.backend.resources.models

import com.fasterxml.jackson.annotation.ObjectIdGenerator
import com.fasterxml.jackson.annotation.ObjectIdResolver

import blgoify.backend.resources.Article
import blgoify.backend.resources.Comment
import blgoify.backend.resources.User
import blgoify.backend.services.UserService
import blgoify.backend.services.articles.ArticleService
import blgoify.backend.services.articles.CommentService

import kotlinx.coroutines.runBlocking

import java.lang.IllegalStateException
import java.util.*

open class Resource(open val uuid: UUID = UUID.randomUUID()) {

    object ObjectResolver : ObjectIdResolver {

        override fun resolveId(id: ObjectIdGenerator.IdKey?): Any? {
            val uuid = id?.key as UUID

            fun genException(scope: Class<*>, ex: Exception)
                    = IllegalStateException("exception during resource (type: ${scope.simpleName}) resolve with UUID $uuid : ${ex.message}", ex)

            return runBlocking {
                // Necessary since we're interacting with Java cruft
                when (id.scope) {

                    Article::class.java -> {
                        try {
                            return@runBlocking ArticleService.get(uuid).get()
                        } catch (e: Exception) {
                            throw genException(id.scope, e)
                        }
                    }

                    User::class.java -> {
                        try {
                            return@runBlocking UserService.get(uuid).get()
                        } catch (e: Exception) {
                            throw genException(id.scope, e)
                        }
                    }

                    Comment::class.java -> {
                        try {
                            return@runBlocking CommentService.get(uuid).get()
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

}