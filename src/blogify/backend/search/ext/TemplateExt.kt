package blogify.backend.search.ext

import blogify.backend.annotations.search.SearchDefaultSort
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.search.models.Template

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

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

private const val TEMPLATE_DEFAULT_DSF = "dsf"

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._buildSearchTemplate(): Template<R> {
    return Template (
        klass  = this,
        name   = this.simpleName!!,
        defaultSortingField = this.cachedPropMap().ok().values
            .filter { it.property.findAnnotation<SearchDefaultSort>() != null }
            .toSet().first().name,
        queryByParams = when { // Temp.
            this.simpleName!!.toLowerCase().contains("article") -> "content,title"
            this.simpleName!!.toLowerCase().contains("user") -> "name,username"
            else -> error("Bruh")
        }
    )
}

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._rebuildSearchTemplate(): Template<R> {

    val template =  this._buildSearchTemplate()

    templateCache[this] = template
    return template
}