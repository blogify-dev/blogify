package blogify.backend.resources.user.handling

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.auth.handling.autenticated
import blogify.backend.database.handling.query
import blogify.backend.database.tables.Users
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.reflect.update
import blogify.backend.resources.user.UserSettings
import blogify.backend.util.*
import blogify.reflect.Dto

import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

/**
 * Request handler for fetching user settings
 */
@BlogifyDsl
val getSettings: RequestContextFunction<Unit> = {
    autenticated { user ->
        val userSettings = query {
            Users.slice(Users.settings)
                .select { Users.uuid eq user.uuid }
                .single().let { it[Users.settings] }
        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't get settings data")

        call.respond(userSettings)
    }
}

/**
 * Request handler for updating user settings
 */
@BlogifyDsl
val updateSettings: RequestContextFunction<Unit> = {
    autenticated { user ->
        val oldSettings = query {
            Users.slice(Users.settings)
                .select { Users.uuid eq user.uuid }
                .single().let { it[Users.settings] }
        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't get settings data")

        val newSettings = Wrap {
            val updateObject = call.receive<Dto>().mappedByHandles(UserSettings::class).get()

            oldSettings.update(updateObject, this).get()
        }.getOrPipelineError(HttpStatusCode.BadRequest, "bad update object")

        val updated = query {
            Users.update({ Users.uuid eq user.uuid }) {
                it[settings] = newSettings
            }
        }.assertGet()

        when (updated) {
            1 -> call.respond(HttpStatusCode.OK)
            0 -> call.respond(HttpStatusCode.InternalServerError, "couldn't update settings")
            else -> never
        }
    }
}
