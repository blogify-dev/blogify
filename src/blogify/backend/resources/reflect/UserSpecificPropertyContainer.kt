package blogify.backend.resources.reflect

import blogify.backend.resources.user.User
import reflectify.computed.models.ComputedPropContainer
import reflectify.models.Mapped

@ExperimentalStdlibApi
class UserSpecificPropertyContainer<TMapped : Mapped, TProperty : Any> (
    override val obj: TMapped,
    val resolve: (TMapped, User) -> TProperty
) : ComputedPropContainer<TMapped, TProperty>()
