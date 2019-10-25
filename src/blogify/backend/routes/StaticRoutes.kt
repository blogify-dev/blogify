package blogify.backend.routes

import blogify.backend.database.Uploadables
import blogify.backend.database.handling.query
import blogify.backend.resources.User
import blogify.backend.resources.static.fs.StaticFileHandler
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.routes.handling.pipeline
import blogify.backend.routes.handling.pipelineError
import blogify.backend.routes.handling.uploadToResource
import blogify.backend.services.UserService

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post

import org.jetbrains.exposed.sql.deleteWhere

import com.github.kittinunf.result.coroutines.failure

fun Route.static() {

    post("/testupload/{uuid}") {
        uploadToResource<User> (
            fetch         = UserService::get,
            modify        = { r, h -> r.copy(profilePicture = h) },
            update        = UserService::update,
            authPredicate = { _ -> true }
        )
    }

    get("/get/{uploadableId}") {

        pipeline("uploadableId") { (uploadableId) ->

            val actualId = uploadableId.takeWhile(Char::isDigit) // Allow for trailing extensions

            val data = StaticFileHandler.readStaticResource(actualId.toLong())

            call.respondBytes(data.bytes, data.contentType)
        }

    }

    delete("/delete/{uploadableId}") {

        pipeline("uploadableId") { (uploadableId) ->

            // VERY TEMP
            val handle = StaticResourceHandle.Ok(ContentType.Any, uploadableId)

            // Delete in DB
            query {
                Uploadables.deleteWhere { Uploadables.fileId eq uploadableId }
            }.failure { pipelineError(HttpStatusCode.InternalServerError, "couldn't delete static resource from db") }

            // Delete in FS
            if (StaticFileHandler.deleteStaticResource(handle)) {
                call.respond(HttpStatusCode.OK)
            } else pipelineError(HttpStatusCode.InternalServerError, "couldn't delete static resource file")

        }

    }

}
