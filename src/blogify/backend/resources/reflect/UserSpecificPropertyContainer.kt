package blogify.backend.resources.reflect

import blogify.backend.resources.user.User
import reflectr.computed.models.ComputedPropContainer
import reflectr.models.Mapped

@ExperimentalStdlibApi
class UserSpecificPropertyContainer<TMapped : Mapped, TProperty : Any> (
    override val obj: TMapped,
    val resolve: (TMapped, User) -> TProperty
) : ComputedPropContainer<TMapped, TProperty>()
