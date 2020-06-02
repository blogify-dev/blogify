package blogify.reflect.entity.slicer

import blogify.reflect.entity.metadata.entity
import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.models.Mapped

import kotlin.reflect.KProperty1

/**
 * Represents a value of [TProperty] extracted from an instance of it's containing class.
 *
 * @author Benjozork
 */
sealed class SlicedProperty<out TProperty : Any?> {

    /**
     * Represents an available value of the property
     *
     * @see SlicedProperty
     */
    class Value<out TProperty : Any>(val value: TProperty) : SlicedProperty<TProperty>()

    /**
     * Represents an available nullable value of the property
     *
     * @see SlicedProperty
     */
    class NullableValue<out TProperty : Any?>(val value: TProperty?) : SlicedProperty<TProperty>()

    /**
     * Represents an unavailable value of the property
     *
     * @see SlicedProperty
     */
    object NotFound : SlicedProperty<Nothing>()

    companion object {

        fun <TObject : Mapped, TProperty : Any> from (
            instance: TObject,
            property: KProperty1<TObject, TProperty?>
        ): SlicedProperty<TProperty> {
            val propDescriptor = property.descriptor

            return when {
                property.returnType.isMarkedNullable -> when {
                    propDescriptor.entity.isVisible -> NullableValue(
                        property.get(instance)
                    )
                    else -> NotFound
                }
                else -> when {
                    propDescriptor.entity.isVisible -> Value(
                        property.get(instance)!!
                    )
                    else -> NotFound
                }
            }
        }

    }

}
