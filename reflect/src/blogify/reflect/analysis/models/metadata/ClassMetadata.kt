package blogify.reflect.analysis.models.metadata

import blogify.reflect.models.Mapped

import kotlin.reflect.KClass

interface ClassMetadata : Metadata<KClass<out Mapped>>
