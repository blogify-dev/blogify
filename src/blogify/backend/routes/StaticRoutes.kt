package blogify.backend.routes

import blogify.backend.resources.User
import blogify.backend.routes.handling.uploadToResource
import blogify.backend.services.UserService

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

import org.slf4j.LoggerFactory

import java.io.File


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

    get("/get/{collection}/{uploadableId}") {

        call.parameters["collection"]?.let { collection ->

            call.parameters["uploadableId"]?.let { id ->

                val uId = id.toUpperCase()

                val file = File("/var/static/$collection-$uId.bin")
                val rawBytes = file.readBytes().drop(STATIC_CONTENT_FILE_SIGNATURE.size)
                val contentType = String(rawBytes.takeWhile { it != 0x00.toByte() }.toByteArray())
                val content = rawBytes.dropWhile { it != 0x00.toByte() }.drop(1).toByteArray()

                call.respondBytes(content, ContentType.parse(contentType))

            }

        }

    }

}
