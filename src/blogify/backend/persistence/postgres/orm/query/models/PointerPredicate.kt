package blogify.backend.persistence.postgres.orm.query.models

import blogify.backend.persistence.postgres.orm.extensions.mapping
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*

enum class Op {
    Less, LessOrEquals, Equals, Greater, GreaterOrEquals
}

class PointerPredicate<TLeftContainer : Resource, TLeft : Any, TRight : TLeft> (
    private val lhs: Pointer<*, TLeftContainer, TLeft>,
    private val op: Op,
    private val rhs: Expression<TRight>
) {

    fun toExpr(): Expression<Boolean> = when (op) {
        Op.Less            -> LessOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.LessOrEquals    -> LessEqOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.Equals          -> EqOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.Greater         -> GreaterOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
        Op.GreaterOrEquals -> GreaterEqOp((lhs.handle.mapping as PropertyMapping.ValueMapping).column, rhs)
    }

}
