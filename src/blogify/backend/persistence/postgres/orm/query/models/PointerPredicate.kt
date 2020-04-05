package blogify.backend.persistence.postgres.orm.query.models

import blogify.backend.persistence.postgres.orm.extensions.klass
import blogify.backend.persistence.postgres.orm.extensions.mapping
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*

import java.util.*

/**
 * Represents a comparison operation between a [Pointer] and a value `<: Pointer's TLeft`.
 */
enum class Op {
    Less, LessOrEquals, Equals, NotEquals, Greater, GreaterOrEquals
}

/**
 * Represents a comparison operation between a [Pointer] and a value `<: Pointer's TLeft (<: Resource)`.
 */
enum class ResourceOp {
    Equals, NotEquals
}

/**
 * Container for a simple [operation][Op] predicate on a [Pointer].
 *
 * This should *not* be used directly by DSL implementations.
 *
 * @param TLeftContainer the `TContainer` type of the pointer on [lhs]
 * @param TLeft          the `TValue` type of the pointer on [lhs], and the supertype of [TRight]
 * @param TRight         the type of the value on [rhs], a subtype of [TLeft]
 *
 * @param lhs the left-hand pointer of the operation. Must **not** point to a [Resource] property.
 * @param op  the [operation][Op] to be run
 * @param rhs the right-hand value of the operation
 *
 * @author Benjozork
  */
class PointerPredicate<TLeftContainer : Resource, TLeft : Any, TRight : TLeft> (
    private val lhs: Pointer<*, TLeftContainer, TLeft>,
    private val op: Op,
    private val rhs: Expression<TRight>
) {

    /**
     * Converts a [PointerPredicate] to an [`Expresion<Boolean>`][Expression]
     */
    fun toExpr(): Expression<Boolean> {
        val column = (lhs.handle.mapping as PropertyMapping.ValueMapping).column
        val table = column.table.alias("joined_ptr_${lhs.parent.hashCode().toString(16).replace('-', 'z')}")
        val realColumn = table[column]

        return when (op) {
            Op.Less            -> LessOp(realColumn, rhs)
            Op.LessOrEquals    -> LessEqOp(realColumn, rhs)
            Op.Equals          -> EqOp(realColumn, rhs)
            Op.NotEquals       -> NeqOp(realColumn, rhs)
            Op.Greater         -> GreaterOp(realColumn, rhs)
            Op.GreaterOrEquals -> GreaterEqOp(realColumn, rhs)
        }
    }

}

/**
 * Container for a simple [operation][Op] predicate on a [Pointer].
 *
 * This should *not* be used directly by DSL implementations.
 *
 * @param TLeftContainer the `TContainer` type of the pointer on [lhs]
 * @param TLeft          the `TValue` type of the pointer on [lhs
 *
 * @param lhs the left-hand pointer of the operation. Must **not** point to a [Resource] property.
 * @param op  the [operation][Op] to be run
 * @param rhs the right-hand value of the operation
 *
 * @author Benjozork
 */
class ResourcePointerPredicate<TLeftContainer : Resource, TLeft : Any> (
    private val lhs: Pointer<*, TLeftContainer, TLeft>,
    private val op: ResourceOp,
    private val rhs: Expression<UUID>
) {

    /**
     * Converts a [PointerPredicate] to an [`Expresion<Boolean>`][Expression]
     */
    fun toExpr(): Expression<Boolean> {
        val lhsContainerUuidColumn = (lhs.handle.mapping as PropertyMapping.AssociativeMapping<*>)
            .leftAssociationColumn

        return when (op) {
            ResourceOp.Equals    -> EqOp(lhsContainerUuidColumn, rhs)
            ResourceOp.NotEquals -> NeqOp(lhsContainerUuidColumn, rhs)
        }
    }

}
