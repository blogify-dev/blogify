package blgoify.backend.database.tables

import org.jetbrains.exposed.sql.Table

object Articles : Table() {

    val uuid      = uuid    ("uuid").primaryKey()
    val title     = varchar ("title", 512)
    val createdAt = long    ("created_at")

}