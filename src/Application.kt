package me.benjozork

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.auth.*
import io.ktor.jackson.*
import io.ktor.features.*

import com.fasterxml.jackson.databind.*

import me.benjozork.routes.articles
import me.benjozork.routes.users

import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.mainModule(@Suppress("UNUSED_PARAMETER") testing: Boolean = false) {

    install(Authentication) {

    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(CallLogging) {
        level = Level.TRACE
    }

    routing {
        route("/api") {
            articles()
            users()
        }
    }
}