package blogify.backend.routes

import blogify.backend.database.Uploadables
import blogify.backend.database.handling.query
import blogify.backend.resources.models.eqr
import blogify.backend.resources.static.fs.StaticFileHandler
import blogify.backend.routes.pipelines.CallPipeLineFunction
import blogify.backend.routes.handling.uploadToResource
import blogify.backend.routes.pipelines.handleAuthentication
import blogify.backend.routes.pipelines.pipeline
import blogify.backend.routes.pipelines.pipelineError
import blogify.backend.services.UserService

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

import com.github.kittinunf.result.coroutines.failure
import com.github.kittinunf.result.coroutines.map

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
