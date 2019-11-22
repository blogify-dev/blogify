package blogify.backend.search.ext

import blogify.backend.annotations.NoSearch
import blogify.backend.annotations.SearchDefaultSort
import blogify.backend.annotations.NoSlice
import blogify.backend.resources.models.Resource
import blogify.backend.search.models.Template
import blogify.backend.search.models.Template.Field.Companion.tsaLogger

import com.andreapivetta.kolor.magenta
import com.andreapivetta.kolor.yellow

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
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

private const val TEMPLATE_DEFAULT_DSF = "_dsf_jank"

@Suppress("FunctionName")
fun <R : Resource> KClass<R>._buildSearchTemplate(): Template<R> {
    val fields = this.declaredMemberProperties
        .filter { it.findAnnotation<NoSearch>() == null && it.findAnnotation<NoSlice>() == null }.toTypedArray()
    val sortingFieldName = fields
        .firstOrNull { it.findAnnotation<SearchDefaultSort>() != null }?.name
            .also { tsaLogger.trace("found DSF for template '${this.simpleName!!}': '$it'".magenta()) }
        ?: TEMPLATE_DEFAULT_DSF
            .also { tsaLogger.trace("generated _dsf_jank for template '${this.simpleName!!}'".yellow()) }
    return Template (
        klass  = this,
        name   = this.simpleName!!,
        fields = fields,
        defaultSortingField = sortingFieldName
    )
}
