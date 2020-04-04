@file:Suppress("UNCHECKED_CAST")

package blogify.backend.testutils

import blogify.backend.persistence.postgres.orm.models.OrmTable

import org.jetbrains.exposed.sql.*

import com.andreapivetta.kolor.*

object SqlUtils {

    fun dumpJoin(join: Join, verbose: Boolean = true): String {
        val stream = StringBuilder()

        stream.append("[Join of ${dumpColumnSetName(join.table)}]\n")
        stream.append("|  Fields: ${
        join.fields.groupBy { (it as Column<*>).table }.values
            .flatMap { reorderFields(it) }
            .map { dumpExpression(it, verbose) }
        }")

        return stream.toString()
    }

    fun dumpColumnSetName(set: ColumnSet) =
        when (set) {
            is Table -> set.tableName.lightMagenta()
            is Alias<Table> -> "<${set.alias.lightYelllow()}>"
            else -> "<can't render column set name>".lightRed()
        }

    fun reorderFields(input: List<Expression<*>>): List<Expression<*>> {
        return if (input.all { it is Column<*> }) {
            (input as List<Column<*>>)
                .sortedBy { if (it.indexInPK != null) 0 else 1 }
        } else input
    }

    fun dumpExpression(expression: Expression<*>, verbose: Boolean = false, showColumnTypes: Boolean = true): String {
        return when (expression) {
            is Column<*>      -> dumpColumn(expression, verbose, showColumnTypes)
            is ComparisonOp   -> "${dumpExpression(expression.expr1, showColumnTypes = false)} ${expression.opSign} ${dumpExpression(expression.expr2)}"
            is QueryParameter -> "'${expression.value}'".lightRed()
            else -> "???_cant_render_???".lightRed()
        }
    }

    fun dumpColumn(column: Column<*>, verbose: Boolean = false, showType: Boolean = true): String =
        (if (verbose) "${dumpColumnSetName(column.table)}(" else "") +
                column.let { when {
                    it.indexInPK != null   -> it.name.lightRed()
                    it.columnType.nullable -> "${it.name}?".lightBlue()
                    else -> it.name.lightGreen()
                } } +
                (if (verbose) "): " else if (showType) ": " else "") +
                (if (showType) column.columnType::class.simpleName?.cyan() else "")

    fun dumpFk(forTable: Table, fk: ForeignKeyConstraint): String {
        val fromTableName = fk.from.table.tableName

        return "${if (fromTableName == forTable.tableName) "self".lightMagenta() else fromTableName.lightMagenta()}(${fk.from.name.lightGreen()})" +
                " -> ${fk.target.table.tableName.lightMagenta()}(${fk.target.name.lightGreen()})"
    }

    fun dumpTable(table: Table): String {
        val stream = StringBuilder()

        stream.append("[Table ${table.tableName.lightMagenta()}]")
        stream.append("\n|  Columns : " + (reorderFields(table.columns) as List<Column<*>>).map { dumpColumn(it) })
        stream.append("\n|  FKs :     " +
                table.columns
                    .mapNotNull { it.foreignKey }
                    .map { dumpFk(table, it) }
                    .ifEmpty { "[${"<none>".yellow()}]" }
        )
        return stream.toString()
    }

    fun dumpOrmTable(table: OrmTable<*>): String {
        val stream = StringBuilder()

        stream.append(dumpTable(table))
        stream.append("\n|  Dep. tables : " +
                if (table.dependencyTables.isNotEmpty())
                    table.dependencyTables.joinToString(prefix = "\n") { dumpTable(it).prependIndent("|\t\t") }
                else "<none>".yellow()
        )

        return stream.toString()
    }

}
