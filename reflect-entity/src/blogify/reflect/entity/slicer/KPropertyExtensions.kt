package blogify.reflect.entity.slicer

import blogify.reflect.models.Mapped

import kotlin.reflect.KProperty1

/**
 * Returns a [SlicedProperty] representing the value of [this] on [instance]
 *
 * @author Benjozork
 */
inline infix fun <TMapped : Mapped, reified TProperty : Any> KProperty1<TMapped, TProperty>.on(instance: TMapped): SlicedProperty<TProperty> =
    SlicedProperty.from(instance, this)

/**
 * Returns a [SlicedProperty] representing the value of [this] on [instance]
 *
 * @author Benjozork
 */
inline infix fun <TMapped : Mapped, reified TProperty : Any> KProperty1<TMapped, TProperty?>.onNullable(instance: TMapped): SlicedProperty<TProperty?> =
    SlicedProperty.from(instance, this)

