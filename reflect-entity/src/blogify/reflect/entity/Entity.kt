package blogify.reflect.entity

import blogify.reflect.models.Mapped

import java.util.*

abstract class Entity(open val uuid: UUID = UUID.randomUUID()) : Mapped()
