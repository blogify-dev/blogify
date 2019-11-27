package blogify.backend.search.autogen

import blogify.backend.annotations.search.NoSearch
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.search.models.Template
import blogify.backend.util.filterThenMapValues

import com.andreapivetta.kolor.green
import kotlin.reflect.KClass

import kotlin.reflect.full.findAnnotation

object AutogenClassVisitor {

    fun <R : Resource> visitAndMapClass(klass: KClass<R>): Set<Template.Field> {
        return klass
            .cachedPropMap()
            .ok()
            .filterThenMapValues (
                predicate = {
                    it.property.findAnnotation<NoSearch>() == null
                            && it.name !== "uuid"
                }, mapper = {
                    AutogenPropertyVisitor.visitAndMapProperty<R>(it.value)
                }
            ).values.toSet()
    }

}