package blogify.backend.testutils

import blogify.backend.persistence.postgres.orm.models.OrmTable

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.Table

import com.andreapivetta.kolor.*

fun dumpExpression(expression: Expression<*>, verbose: Boolean = false): String {
    return when (expression) {
        is Column<*> -> dumpColumn(expression, verbose)
        else -> "_$expression"
    }
}

fun dumpColumn(column: Column<*>, verbose: Boolean = false): String =
        (if (verbose) "${column.table.tableName.lightMagenta()}(" else "") +
                column.let { when {
                    it.indexInPK != null   -> it.name.lightRed()
                    it.columnType.nullable -> "${it.name}?".lightBlue()
                    else -> it.name.lightGreen()
                } } +
                (if (verbose) "): " else ": ") +
                column.columnType::class.simpleName?.cyan()

fun dumpFk(forTable: Table, fk: ForeignKeyConstraint): String {
    val fromTableName = fk.from.table.tableName

    return "${if (fromTableName == forTable.tableName) "self".lightMagenta() else fromTableName.lightMagenta()}(${fk.from.name.lightGreen()})" +
            " -> ${fk.target.table.tableName.lightMagenta()}(${fk.target.name.lightGreen()})"
}

fun dumpTable(table: Table): String {
    val stream = StringBuilder()

    stream.append("[Table ${table.tableName.lightMagenta()}]")
    stream.append("\n|  Columns : " + table.columns.sortedBy { if (it.indexInPK != null) 0 else 1 }.map { dumpColumn(it) })
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
