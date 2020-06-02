package blogify.reflect.entity.metadata

import blogify.reflect.analysis.models.PropertyDescriptor

val PropertyDescriptor.entity get() =
    this.getOrMake(EntityMetadata.Provider)
