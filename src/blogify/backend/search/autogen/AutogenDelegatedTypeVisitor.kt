package blogify.backend.search.autogen

import blogify.backend.search.annotations.DelegatedSearchReceiver

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object AutogenDelegatedTypeVisitor {

    fun visitAndFindDelegate(klass: KClass<*>): KProperty1<*, *> {
        return klass.declaredMemberProperties.firstOrNull { it.findAnnotation<DelegatedSearchReceiver>() != null }
            ?: error("couldn't find delegated search field receiver in class '${klass.simpleName}'")
    }

}
