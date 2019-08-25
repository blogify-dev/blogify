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

import java.util.*

open class Resource(open val uuid: UUID = UUID.randomUUID()) {

    object ObjectResolver : ObjectIdResolver {

        override fun resolveId(id: ObjectIdGenerator.IdKey?): Any? {
            val uuid = id?.key as UUID

            return runBlocking {
                // Necessary since we're interacting with Java cruft
                when (id.scope) {

                    Article::class.java -> {
                        return@runBlocking ArticleService.get(uuid)
                    }

                    User::class.java -> {
                        return@runBlocking UserService.get(uuid)
                    }

                    Comment::class.java -> {
                        return@runBlocking CommentService.get(uuid)
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