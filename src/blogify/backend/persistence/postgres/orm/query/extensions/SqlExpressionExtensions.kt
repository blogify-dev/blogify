package blogify.backend.persistence.postgres.orm.query.extensions

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder

fun <TOperation> expr(body: SqlExpressionBuilder.() -> Op<TOperation>): Op<TOperation> = body(SqlExpressionBuilder)
