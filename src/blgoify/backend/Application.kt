package blgoify.backend

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.auth.*
import io.ktor.jackson.*
import io.ktor.features.*

import com.fasterxml.jackson.databind.*

import blgoify.backend.routes.articles.articles
import blgoify.backend.routes.users
import blgoify.backend.database.Database
import blgoify.backend.database.Articles
import blgoify.backend.database.Comments
import blgoify.backend.database.Users
import blgoify.backend.routes.auth
import blgoify.backend.util.query

import org.jetbrains.exposed.sql.SchemaUtils

import kotlinx.coroutines.runBlocking

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

    // Initialize database
    //test

    Database.init()

    runBlocking { query {
        SchemaUtils.create (
            Articles,
            Articles.Content,
            Users,
            Comments
        )
    }}

    routing {
        route("/api") {
            articles()
            users()
            auth()
        }
    }
}
