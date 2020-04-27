package blogify.backend.search.ext

import annotations.search.QueryByField
import annotations.search.SearchDefaultSort
import blogify.backend.resources.models.Resource
import reflect.cachedPropMap
import reflect.models.extensions.ok
import blogify.backend.search.models.Template

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

private val templateCache: MutableMap<KClass<*>, Template<*>> = mutableMapOf()

@Suppress("ObjectPropertyName", "UNCHECKED_CAST")
val <R : Resource> KClass<R>._searchTemplate: Template<R> get() {
    var cached: Template<R>? = templateCache[this] as? Template<R>?
    if (cached == null) {
        cached = this._buildSearchTemplate()
        templateCache[this] = cached
    }

    return cached
}

const val TEMPLATE_DEFAULT_DSF = "_dsf"

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._buildSearchTemplate(): Template<R> {
    return Template (
        klass  = this,
        name   = this.simpleName!!,
        defaultSortingField = this.cachedPropMap().ok().values
            .filter { it.property.findAnnotation<SearchDefaultSort>() != null }
            .toSet().firstOrNull()?.name ?: TEMPLATE_DEFAULT_DSF, // Generate TEMPLATE_DEFAULT_DSF if there is no annotated DSF
        queryByParams = this.cachedPropMap().ok().values
            .filter { it.property.findAnnotation<QueryByField>() != null }
            .toSet().joinToString(separator = ",") { it.name }
    )
}

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._rebuildSearchTemplate(): Template<R> {

    val template =  this._buildSearchTemplate()

    templateCache[this] = template
    return template
}
