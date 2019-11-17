package blogify.backend.search.ext

import blogify.backend.annotations.NoSearch
import blogify.backend.annotations.SearchDefaultSort
import blogify.backend.annotations.NoSlice
import blogify.backend.resources.models.Resource
import blogify.backend.search.models.Template

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

private val templateCache: MutableMap<KClass<*>, Template<*>> = mutableMapOf()

@Suppress("ObjectPropertyName", "UNCHECKED_CAST")
val <R : Resource> KClass<R>._template: Template<R> get() {
    var cached: Template<R>? = templateCache[this::class] as? Template<R>?
    if (cached == null) {
        cached = this._buildTemplate()
        templateCache[this::class] = cached
    }

    return cached
}

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._buildTemplate(): Template<R> {
    val fields = this.declaredMemberProperties
        .filter { it.findAnnotation<NoSearch>() == null && it.findAnnotation<NoSlice>() == null }.toTypedArray()
    return Template (
        klass  = this,
        name   = this.simpleName!!,
        fields = fields,
        defaultSortingField = (fields.firstOrNull { it.findAnnotation<SearchDefaultSort>() != null } ?: fields.first()).name
    )
}
