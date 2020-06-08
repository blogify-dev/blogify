package blogify.reflect.entity.instantiation

import blogify.reflect.Dto

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

/**
 * Utility for resolving a sealed subtype of a class using properties present in a [Dto].
 *
 * @author Benjozork
 */
internal object PolymorphicTypeResolver {

    /**
     * Finds a sealed subtype of [sealedClass] as candidate for instantiation using the data in [Dto].
     *
     * This basically looks for properties in [dto] that are not declared in the superclass to find the right
     * type the Dto could instantiate.
     *
     * Throws an [IllegalStateException] if there are zero or multiple matching types with the given properties.
     *
     * @param dto         the data to look for properties in
     * @param sealedClass the parent superclass to look for sealed subtypes into
     */
    fun <TProperty : Any> resolveType(dto: Dto, sealedClass: KClass<TProperty>): KClass<out TProperty> {
        val sealedSubclasses = sealedClass.sealedSubclasses

        if (sealedSubclasses.isEmpty()) error("class '${sealedClass.qualifiedName}' is not sealed or has no subclasses")

        val commonPropertyNames = sealedClass.declaredMemberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .map { it.name }

        val nonCommonDtoProps = dto.keys.filter { it !in commonPropertyNames }

        return sealedSubclasses.filter { it.memberProperties.map { p -> p.name }.containsAll(nonCommonDtoProps) }
            .let {
                when {
                    it.size == 1 -> it.first()
                    it.isEmpty() -> error("no matching type with properties ${dto.keys}")
                    else -> error("multiple matching types with properties ${dto.keys}")
                }
            }
    }

}
