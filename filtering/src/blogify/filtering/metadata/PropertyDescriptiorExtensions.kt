package blogify.filtering.metadata

import blogify.reflect.analysis.models.PropertyDescriptor

val PropertyDescriptor.filtering get() =
    this.getOrMake(FilteringMetadata.Provider)
