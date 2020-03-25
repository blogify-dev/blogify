package blogify.backend.persistence.postgres.orm.models

import org.jetbrains.exposed.sql.Table

open class SimpleOrmTable(override var primaryKey: PrimaryKey? = null, name: String) : Table(name)
