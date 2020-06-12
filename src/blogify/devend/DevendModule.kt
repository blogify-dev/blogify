package blogify.devend

import blogify.backend.bootstrap.BlogifyApplicationBootstrapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Application.blogifyDevModule(configuration: BlogifyApplicationBootstrapper.StartConfiguration) {
    routing {
        route("/dev/") {
            get {
                call.respond("Devend works")
            }
        }
    }
}
