package blogify.backend.resources.reflect.extensions

import blogify.backend.entity.Resource
import blogify.backend.resources.reflect.UserSpecificPropertyContainer
import blogify.backend.resources.user.User

import kotlinx.coroutines.runBlocking

fun <TResource : Resource, TProperty : Any> TResource.userSpecific(initializer: suspend (User) -> TProperty) =
    UserSpecificPropertyContainer(this) { _, u ->
        runBlocking { initializer(u) }
    }
