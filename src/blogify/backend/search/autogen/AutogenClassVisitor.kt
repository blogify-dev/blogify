package blogify.backend.search.autogen

import blogify.reflect.annotations.search.NoSearch
import blogify.reflect.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.reflect.propMap
import blogify.reflect.models.extensions.ok
import blogify.backend.search.models.Template
import blogify.backend.util.filterThenMapValues

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object AutogenClassVisitor {

    fun <R : Resource> visitAndMapClass(klass: KClass<R>): Set<Template.Field> {
        return klass
            .propMap
            .ok()
            .filterThenMapValues (
                predicate = {
                    it.property.findAnnotation<NoSearch>() == null && it.property.findAnnotation<Computed>() == null
                            && it.name !== "uuid"
                }, mapper = {
                    AutogenPropertyVisitor.visitAndMapProperty<R>(it.value)
                }
            ).values.toSet()
    }

}
