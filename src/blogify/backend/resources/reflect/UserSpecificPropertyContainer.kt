package blogify.backend.resources.reflect

import blogify.backend.resources.user.User
import blogify.reflect.computed.models.ComputedPropContainer
import blogify.reflect.models.Mapped

class UserSpecificPropertyContainer<TMapped : Mapped, TProperty : Any> (
    override val obj: TMapped,
    val resolve: (TMapped, User) -> TProperty
) : ComputedPropContainer<TMapped, TProperty>()
