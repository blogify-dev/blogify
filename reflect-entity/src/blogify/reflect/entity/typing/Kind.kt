package blogify.reflect.entity.typing

/**
 * Represents an abstracted, low-level and language-agnostic type for entity properties.
 *
 * @property type  the actual type identifier
 * @property array whether or not the type is an array type or not
 *
 * @author Benjozork
 */
data class Kind (
    val type: Type,
    val array: Boolean
) {

    @Suppress("EnumEntryName")
    enum class Type {
        String,
        Number,
        Boolean,
        Entity
    }

    override fun toString() = type.name.toLowerCase() + if (array) "[]" else ""

}
