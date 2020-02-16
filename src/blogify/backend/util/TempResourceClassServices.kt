@file:Suppress("unused")

package blogify.backend.util

import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.services.UserRepository
import blogify.backend.services.ArticleRepository
import blogify.backend.services.CommentRepository
import blogify.backend.services.models.Repository

import kotlin.reflect.KClass

// TODO: remove this
val KClass<Article>.service: ArticleRepository
    get() = ArticleRepository

// TODO: remove this
val KClass<User>.service: UserRepository
    get() = UserRepository

// TODO: remove this
val KClass<Comment>.service: CommentRepository
    get() = CommentRepository

// TODO: remove this
@Suppress("UNCHECKED_CAST")
val <R : Resource> KClass<R>.repository: Repository<R>
    get() {
        return when {
            this == Article::class -> {
                Article::class.service as Repository<R>
            }
            this == User::class -> {
                User::class.service as Repository<R>
            }
            this == Comment::class -> {
                Comment::class.service as Repository<R>
            }
            else -> error("fuck")
        }
    }
