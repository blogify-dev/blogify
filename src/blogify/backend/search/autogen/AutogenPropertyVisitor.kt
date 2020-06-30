package blogify.backend.search.autogen

import blogify.backend.search.annotations.DelegatedSearch
import blogify.backend.entity.Resource
import reflectr.models.PropMap
import blogify.backend.search.models.Template
import blogify.util.never
import reflectr.extensions.klass
import reflectr.extensions.safeKlass

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

import com.andreapivetta.kolor.green

import org.slf4j.LoggerFactory

@ExperimentalStdlibApi
object AutogenPropertyVisitor {

    private val logger = LoggerFactory.getLogger("blogify-typesense-autogen")

    val fieldTypes by lazy {
        Template.Field::class.sealedSubclasses
            .filter { it.findAnnotation<Template.Field.TypesenseFieldType>() != null }
            .associateWith { it.findAnnotation<Template.Field.TypesenseFieldType>()!! }
            .also { Template.Field.tsaLogger.debug("mapped field subclasses".green()) }
    }

    fun <R : Resource> visitAndMapProperty(handle: PropMap.PropertyHandle.Ok): Template.Field {
        val property = handle.property
        val propertyClass = property.returnType.safeKlass<Resource>() ?: never
        val typeAnnotations = property.returnType.annotations

        // Is it delegated ?
        return if (typeAnnotations.any { it.annotationClass == DelegatedSearch::class }) {
            val delegateProperty = AutogenDelegatedTypeVisitor.visitAndFindDelegate(propertyClass)
            val delegatePropertyFieldType = getVisitedPropertyFieldType(delegateProperty)
                ?: error("invalid delegated property '${delegateProperty.name}' (delegated from '${property.name}') type")

            logger.trace("created typesense field for property '${property.name}' (delegated to '${delegateProperty.name}'): assigned type ${delegatePropertyFieldType.simpleName}".green())

            // Call constructor with delegate
            delegatePropertyFieldType.constructors.first().call(property.name, false, delegateProperty)
        } else {
            val propertyFieldType = getVisitedPropertyFieldType(property)
                ?: error("invalid property '${property.name}' type")

            logger.trace("created typesense field for property '${property.name}': assigned type ${propertyFieldType.simpleName}".green())

            // Call constructor without delegate
            propertyFieldType.constructors.first().call(property.name, false, null)
        }
    }

    private fun getVisitedPropertyFieldType(property: KProperty1<*, *>): KClass<out Template.Field>? {
        return fieldTypes.entries.firstOrNull { it.value.type == property.returnType.klass() }?.key
    }

}
