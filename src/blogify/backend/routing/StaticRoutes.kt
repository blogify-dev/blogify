package blogify.backend.routing

import blogify.backend.resources.static.file.StaticFileHandler
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.makeStaticRoutes() {

    get("/get/{uploadableId}") {

        requestContext {

            val uploadableId = param("uploadableId")

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
