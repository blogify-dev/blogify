package blogify.backend.search.autogen

import reflectr.annotations.search.NoSearch
import reflectr.computed.models.Computed
import blogify.backend.entity.Resource
import reflectr.propMap
import reflectr.models.extensions.ok
import blogify.backend.search.models.Template
import blogify.common.util.filterThenMapValues

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@ExperimentalStdlibApi
object AutogenClassVisitor {

    fun <R : Resource> visitAndMapClass(klass: KClass<R>): Set<Template.Field> {
        return klass
            .propMap
            .ok
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
