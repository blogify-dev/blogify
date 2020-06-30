package blogify.backend.database

import krate.handling.query
import blogify.util.Sr

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.JoinType
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
// TODO make use of this in comment count ? maybe ?
private suspend fun <A : Any> countReferences (
    referenceField: Column<A>,
    referenceValue: A,
    where:          SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
): Sr<Int> {
    return query {
        referenceField.table.select { referenceField eq referenceValue and where() }.count().toInt()
    }
}

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
            .toSet().map { it[originField] to it[refCountColumn].toInt() }.toMap()
    }
}

private suspend fun <A : Any, B : Any, C : Any> getAllReferences (
    originField:          Column<A>,
    referenceTargetField: Column<B>,
    returnedTargetField:  Column<C>,
    where:                SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }
) : Sr<Map<A, Set<C>>> {
    return query {
        originField.table.join(
            referenceTargetField.table, JoinType.LEFT,
            onColumn = originField, otherColumn = referenceTargetField
        )
            .slice(originField, returnedTargetField)
            .select(where)
            .map { it[originField] to it.getOrNull(returnedTargetField) }
            .groupBy { it.first }
            .mapValues { it.value.mapNotNull { pair -> pair.second }.toSet() }
    }
}

/**
 * Associates all values of left-hand column to the number of references to them in the right hand column
 *
 * @receiver the column in which to look for values
 * @param    other the column in which to count references to the left-hand column
 *
 * @return a [Map] of values (let `X`) of the left-hand column to the number of references to `X` in [other]
 *
 * @author Benjozork
 */
suspend infix fun <A : Any> Column<A>.countReferredToBy(other: Column<A>) = countAllReferences(this, other).get()

/**
 * Associates all values of the left-hand column to the value of the left-hand column in [others] based on the presence of that first value
 * in the right-hand column of [others]
 *
 * - the first column of [others] is used to look for values of the receiver column
 * - the second column [others] is where the values the receiver will be associated to reside
 *
 * @receiver the column in which to look for values
 * @param    others a [Pair] of columns to use for the search (see above)
 *
 * @return a [Map] of values (let `X`) of the left-hand column (of type [A]) to all values of the second column of [others] in rows where
 *         the first column of [others] matched the current value of `X`
 *
 * @author Benjozork
 *
 */
suspend infix fun <A : Any, B : Any, C : Any> Column<A>.findReferredToBy(others: Pair<Column<B>, Column<C>>) = getAllReferences(this, others.first, others.second).get()
