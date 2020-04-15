package blogify.backend.resources.models

import blogify.backend.events.models.Event
import blogify.backend.events.models.EventType
import blogify.backend.resources.User

import java.util.*

abstract class UserCreatedResource(uuid: UUID) : Resource(uuid) {

    abstract inner class CreationEvent : Event(creator, this, EventType.Activity)

    abstract val creator: User

}
