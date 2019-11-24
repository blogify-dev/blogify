package blogify.backend.util

import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.services.UserService
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.articles.CommentService
import blogify.backend.services.models.Service

import kotlin.reflect.KClass

val KClass<Article>.service: ArticleService
    get() = ArticleService

val KClass<User>.service: UserService
    get() = UserService

val KClass<Comment>.service: CommentService
    get() = CommentService

val <R : Resource> KClass<R>.service: Service<R>
    get() {
        return when {
            this == Article::class -> {
                Article::class.service as Service<R>
            }
            this == User::class -> {
                User::class.service as Service<R>
            }
            this == Comment::class -> {
                Comment::class.service as Service<R>
            }
            else -> error("fuck")
        }
    }
