package blogify.backend.routing

import blogify.backend.pipelines.ApplicationContext
import blogify.backend.resources.static.file.StaticFileHandler
import blogify.backend.routing.pipelines.param
import blogify.backend.routing.pipelines.request
import blogify.backend.routing.pipelines.wrapRequest

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.static(applicationContext: ApplicationContext) {

    get("/get/{uploadableId}") {

        wrapRequest(applicationContext) {
            request {

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

}
