package blogify.reflect.entity.metadata

import blogify.reflect.analysis.models.ClassDescriptor
import blogify.reflect.analysis.models.PropertyDescriptor

val PropertyDescriptor.entity get() =
    this.getOrMake(EntityMetadata.Provider)

val ClassDescriptor.entity get() =
    this.getOrMake(EntityClassMetadata.Provider)
