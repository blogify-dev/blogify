package blogify.backend.search.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import blogify.backend.annotations.search.SearchByUUID
import blogify.backend.resources.models.Resource
import blogify.backend.search.Typesense
import blogify.backend.search.autogen.AutogenClassVisitor

import org.slf4j.LoggerFactory

import com.andreapivetta.kolor.green

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf

@Suppress("unused")
data class Template<T : Resource>(
    @JsonIgnore
    val klass: KClass<T>,

    val name: String,

    @get:JsonProperty("default_sorting_field")
    val defaultSortingField: String,

    val queryByParams: String
) {

    val fields = AutogenClassVisitor.visitAndMapClass(klass)
    val delegatedFields = fields.filter { it.delegatedTo != null }

    /**
     * Represents a [Typesense] field used for indexing documents. Should generally only exist once for a given resource class.
     *
     * @property name  the given name of the field
     * @property type  a type string used to set the type of the field
     * @property facet whether or not the field is a [facet](https://typesense.org/docs/0.11.0/api/#create-collection)
     *
     * @author Benjozork
     */
    @Suppress("unused")
    sealed class Field (
        val name: kotlin.String,
        @get:JsonProperty("type")
        val type: kotlin.String,
        val facet: Boolean,
        @JsonIgnore
        val delegatedTo: KProperty1<Any?, Any?>?
    ) {

        /**
         * Marks a class as reflecting a [Typesense] type
         *
         * @property type       the equivalent Kotlin type for this type
         * @property canBeFacet whether or not this type can be a facet when sent in a template to [Typesense].
         *                      See [the typesense docs](https://typesense.org/docs/0.11.0/api/#create-collection) for details.
         *
         * @author Benjozork
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class TypesenseFieldType(val type: KClass<*>, val canBeFacet: Boolean)

        /** [Typesense] type implementation for [kotlin.String] */
        @TypesenseFieldType(kotlin.String::class, true)
        class String(name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "string", facet, delegatedTo)

        /** [Typesense] type implementation for an array of [kotlin.String] */
        @TypesenseFieldType(Array<kotlin.String>::class, true)
        class StringArray (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "string[]", facet, delegatedTo)

        /** [Typesense] type implementation for [kotlin.Int] */
        @TypesenseFieldType(Int::class, false)
        class Int32 (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any>? = null): Field(name, "int32", facet, delegatedTo)

        /** [Typesense] type implementation for an array of [kotlin.Int] */
        @TypesenseFieldType(Array<Int>::class, false)
        class Int32Array (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "int32[]", facet, delegatedTo)

        /** [Typesense] type implementation for [kotlin.Long] */
        @TypesenseFieldType(Long::class, false)
        class Int64 (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "int64", facet, delegatedTo)

        /** [Typesense] type implementation for an array of [kotlin.Long] */
        @TypesenseFieldType(Array<Long>::class, false)
        class Int64Array (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "int64[]", facet, delegatedTo)

        /** [Typesense] type implementation for [kotlin.Float] */
        @TypesenseFieldType(Float::class, false)
        class Float (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "float", facet, delegatedTo)

        /** [Typesense] type implementation for an array of [kotlin.Float] */
        @TypesenseFieldType(Array<Float>::class, false)
        class FloatArray (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "float[]", facet, delegatedTo)

        /** [Typesense] type implementation for [kotlin.Boolean] */
        @TypesenseFieldType(Boolean::class, false)
        class Bool (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "bool", facet, delegatedTo)

        /** [Typesense] type implementation for an array of [kotlin.Boolean] */
        @TypesenseFieldType(Array<Boolean>::class, false)
        class BoolArray (name: kotlin.String, facet: Boolean = false, delegatedTo: KProperty1<Any?, Any?>? = null): Field(name, "bool[]", facet, delegatedTo)

        companion object {

            val tsaLogger = LoggerFactory.getLogger("blogify-typesense-autogen")

            private val subClassCache by lazy {
                Field::class.sealedSubclasses
                    .filter { it.findAnnotation<TypesenseFieldType>() != null }
                    .associateWith { it.findAnnotation<TypesenseFieldType>()!! }
                    .also { tsaLogger.debug("mapped field subclasses".green()) }
            }

            /**
             * Creates an instance of [Field] from a [KProperty1].
             *
             * @param property the [KProperty1] to create the field from. Must be of a valid types from those listed in [Field].
             * @param facet    whether or not this field should be a facet.
             *                 See [the typesense docs](https://typesense.org/docs/0.11.0/api/#create-collection) for details.
             *
             * @return the created field. Will throw an error if the type of [property] is invalid, or if an error is present in the available
             *         types in [Typesense.Field].
             *
             * @author Benjozork
             */
            fun from(property: KProperty1<*, *>, facet: Boolean = false): Field {
                val name = property.name
                val returnType = property.returnType
                val typesenseFieldClass =
                    if (property.findAnnotation<SearchByUUID>() == null) {
                        subClassCache.entries
                            .firstOrNull { entry ->
                                // Make sure type class exists for given type and allows facet if it wishes to be one
                                if (returnType.isSubtypeOf(Collection::class.createType(listOf(KTypeProjection.STAR))))
                                    returnType.arguments[0].type == entry.value.type && if (!entry.value.canBeFacet) !facet else true
                                else
                                    returnType.classifier == entry.value.type && if (!entry.value.canBeFacet) !facet else true
                            }?.toPair() ?: error("illegal type '$returnType { facet: $facet }' on property '${property.name}'")
                    } else {
                        @Suppress("RemoveRedundantQualifierName")
                        Template.Field.String::class to subClassCache[Template.Field.String::class]
                    }

                tsaLogger.trace("created typesense field for property $name with type ${returnType.classifier.toString()}; assigned type ${typesenseFieldClass.first.simpleName}".green())

                return typesenseFieldClass.first.constructors.first().call(name, facet)
            }

        }

        object Serializer : StdSerializer<Field>(Field::class.java) {
            override fun serialize(value: Field?, gen: JsonGenerator?, provider: SerializerProvider?) = gen!!.writeString(value?.type)
        }

    }

}
