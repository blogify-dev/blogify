package blogify.backend.resources.models

import blogify.backend.events.models.Event
import blogify.backend.resources.User

import java.util.*

abstract class UserCreatedResource(uuid: UUID) : Resource(uuid) {

    inner class CreationEvent : Event(creator, this)

    abstract val creator: User

}
