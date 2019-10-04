package blogify.backend

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing

import com.andreapivetta.kolor.cyan

import com.fasterxml.jackson.databind.*

import blogify.backend.routes.articles.articles
import blogify.backend.routes.users.users
import blogify.backend.database.Database
import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.database.Users
import blogify.backend.routes.auth
import blogify.backend.database.handling.query
import blogify.backend.util.SinglePageApplication

import io.ktor.application.call
import io.ktor.features.Compression
import io.ktor.features.GzipEncoder
import io.ktor.response.respondRedirect
import io.ktor.routing.get

import org.jetbrains.exposed.sql.SchemaUtils

import kotlinx.coroutines.runBlocking

import org.slf4j.event.Level

const val version = "PRX3"

const val asciiLogo = """
    __     __               _  ____      
   / /_   / /____   ____ _ (_)/ __/__  __
  / __ \ / // __ \ / __ `// // /_ / / / /
 / /_/ // // /_/ // /_/ // // __// /_/ / 
/_.___//_/ \____/ \__, //_//_/   \__, /  
                 /____/         /____/   
---- Version $version - Development build -
"""

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.mainModule(@Suppress("UNUSED_PARAMETER") testing: Boolean = false) {

    // Print startup logo

    println(asciiLogo.cyan())

    // Initialize jackson

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    // Intitialize call logging

    install(CallLogging) {
        level = Level.TRACE
    }

    install(SinglePageApplication) {
        folderPath = "/frontend"
    }

    install(Compression) {
        encoder("gzip0", GzipEncoder)
    }

    // Initialize database

    Database.init()

    // Create tables if they don't exist

    runBlocking { query {
        SchemaUtils.create (
            Articles,
            Articles.Categories,
            Users,
            Comments
        )
    }}

    // Initialize routes

    routing {
        route("/api") {
            articles()
            users()
            auth()
        }
        get("/") {
            call.respondRedirect("/home")
        }
    }

}
