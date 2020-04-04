package blogify.backend.persistence.postgres.orm.query.models

import blogify.backend.persistence.postgres.orm.extensions.mapping
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*

/**
 * Represents a comparison operation between a [Pointer] and a value `<: Pointer's TLeft`.
 */
enum class Op {
    Less, LessOrEquals, Equals, Greater, GreaterOrEquals
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
    fun toExpr(): Expression<Boolean> = when (op) {
        Op.Less            -> LessOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.LessOrEquals    -> LessEqOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.Equals          -> EqOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.Greater         -> GreaterOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.GreaterOrEquals -> GreaterEqOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
    }

}
