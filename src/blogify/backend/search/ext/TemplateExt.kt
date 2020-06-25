package blogify.backend.search.ext

import blogify.backend.search.annotations.QueryByField
import blogify.backend.search.annotations.SearchDefaultSort
import blogify.backend.entity.Resource
import blogify.backend.search.models.Template
import reflectr.propMap
import reflectr.models.extensions.ok

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@ExperimentalStdlibApi
private val templateCache: MutableMap<KClass<*>, Template<*>> = mutableMapOf()

@Suppress("ObjectPropertyName", "UNCHECKED_CAST")
@ExperimentalStdlibApi
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
@ExperimentalStdlibApi
fun <R : Resource> KClass<R>._buildSearchTemplate(): Template<R> {
    return Template (
        klass  = this,
        name   = this.simpleName!!,
        defaultSortingField = this.propMap.ok.values
            .filter { it.property.findAnnotation<SearchDefaultSort>() != null }
            .toSet().firstOrNull()?.name ?: TEMPLATE_DEFAULT_DSF, // Generate TEMPLATE_DEFAULT_DSF if there is no annotated DSF
        queryByParams = this.propMap.ok.values
            .filter { it.property.findAnnotation<QueryByField>() != null }
            .toSet().joinToString(separator = ",") { it.name }
    )
}

@Suppress("FunctionName")
@ExperimentalStdlibApi
fun <R : Resource> KClass<R>._rebuildSearchTemplate(): Template<R> {

    val template =  this._buildSearchTemplate()

    templateCache[this] = template
    return template
}
