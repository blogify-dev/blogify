package blogify.backend.util

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

class Tsvector : ColumnType(true) {
    override fun sqlType(): String = "tsvector"
}

fun Table.tsvector(name: String) = this.registerColumn<Tsvector>(name, Tsvector())