package blogify.backend.database

import blogify.backend.services.models.Service
import blogify.backend.database.handling.query
import blogify.backend.util.Sr

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select

/**
 * Counts the number of references of a certain value in a provided column of a table
 *
 * @param referenceField the column in which those references to [referenceValue] are to be counted.
 *
 * @return the number of rows of [referenceField]'s table in which [referenceValue] appears in [referenceField]
 *
 * @author Benjozork
 */
suspend fun <A : Any> countReferences (
    referenceField: Column<A>,
    referenceValue: A,
    where:          SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
): Sr<Int> {
    return query {
        referenceField.table.select { referenceField eq referenceValue and where() }.count()
    }
        .mapError { e -> Service.Exception(e) }
}

/**
 * Counts the number of references for every value of a column in another provided column.
 *
 * @param originField the column in which the values to count references to are stored
 * @param secondField the column in which references to each value of [originField] are stored
 *
 * @return a map of all the values of [originField] to the number of references to that value in [secondField]
 *
 * @author Benjozork
 */
private suspend fun <A : Any> countAllReferences (
    originField: Column<A>,
    secondField: Column<A>,
    where:       SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
): Sr<Map<A, Int>> {
    return query {
        val refCountColumn = secondField.count()
        originField.table
            .leftJoin(secondField.table, { originField }, { secondField })
            .slice(originField, refCountColumn)
            .select(where)
            .groupBy(originField)
            .toSet().map { it[originField] to it[refCountColumn] }.toMap()
    }
}

/**
 * Associates all values of [this] to the number of references to them in [other]
 */
suspend infix fun <A : Any> Column<A>.referredToBy(other: Column<A>) = countAllReferences(this, other).get()