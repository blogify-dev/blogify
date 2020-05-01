package blogify.backend.resources.user.extensions

import blogify.backend.annotations.table
import blogify.backend.database.handling.query
import blogify.backend.database.tables.Users
import blogify.backend.resources.user.User
import blogify.backend.resources.user.UserSettings
import blogify.backend.util.Sr
import blogify.backend.util.WrapBlocking

import org.jetbrains.exposed.sql.select

val User.settings: Sr<UserSettings>
    get() = WrapBlocking { query {
        User::class.table
                .slice(Users.settings)
                .select { Users.uuid eq this@settings.uuid }
                .single().let { it[Users.settings] }
    }.get() }
