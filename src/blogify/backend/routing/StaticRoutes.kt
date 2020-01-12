package blogify.backend.routing

import blogify.backend.resources.static.file.StaticFileHandler
import blogify.backend.routing.pipelines.pipeline

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.static() {

    get("/get/{uploadableId}") {

        pipeline("uploadableId") { (uploadableId) ->

            val actualId = uploadableId.takeWhile(Char::isDigit) // Allow for trailing extensions
            if (actualId != "") {
                val data = StaticFileHandler.readStaticResource(actualId.toLong())

                call.respondBytes(data.bytes, data.contentType)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }

        }

    }

}
