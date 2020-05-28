package blogify.reflect.analysis.impl

import blogify.reflect.analysis.models.PropertyDescriptor

val PropertyDescriptor.base: BaseMetadata
    get() = this.getOrMake(BaseMetadata.Provider)
