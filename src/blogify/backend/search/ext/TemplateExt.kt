package blogify.backend.search.ext

import blogify.backend.resources.models.Resource
import blogify.backend.search.models.Template

import kotlin.reflect.KClass

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
        defaultSortingField = TEMPLATE_DEFAULT_DSF
    )
}

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._rebuildSearchTemplate(): Template<R> {

    val template =  Template (
        klass  = this,
        name   = this.simpleName!!,
        defaultSortingField = TEMPLATE_DEFAULT_DSF
    )

    templateCache[this] = template
    return template
}