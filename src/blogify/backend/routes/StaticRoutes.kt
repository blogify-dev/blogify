package blogify.backend.routes

import blogify.backend.resources.User
import blogify.backend.resources.static.fs.StaticFileHandler
import blogify.backend.routes.handling.pipeline
import blogify.backend.routes.handling.uploadToResource
import blogify.backend.services.UserService

import io.ktor.application.call
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("blogify-static-uploader")

private val STATIC_CONTENT_FILE_SIGNATURE = byteArrayOf(0x6c, 0x75, 0x63, 0x79)

fun Route.static() {

    post("/testupload") {
        uploadToResource<User> (
            fetch  = UserService::get,
            modify = { r, h -> r.copy(profilePicture = h) },
            update = UserService::update
        )
    }

    get("/get/{uploadableId}") {

        pipeline("uploadableId") { (uploadableId) ->
            uploadableId!!
            val data = StaticFileHandler.readStaticResource(uploadableId.toLong())

            call.respondBytes(data.bytes, data.contentType)
        }

    }

}
