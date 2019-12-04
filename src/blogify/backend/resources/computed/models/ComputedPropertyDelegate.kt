package blogify.backend.resources.computed.models

import blogify.backend.resources.models.Resource

import kotlin.properties.ReadOnlyProperty

abstract class ComputedPropertyDelegate<A : Any> : ReadOnlyProperty<Resource, A>
