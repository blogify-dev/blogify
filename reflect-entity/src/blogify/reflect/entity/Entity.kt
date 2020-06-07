package blogify.reflect.entity

import blogify.reflect.models.Mapped

import java.util.*

abstract class Entity(open val uuid: UUID = UUID.randomUUID()) : Mapped() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

}
