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
import blogify.backend.routes.users
import blogify.backend.database.Database
import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.database.Users
import blogify.backend.routes.auth
import blogify.backend.database.handling.query

import org.jetbrains.exposed.sql.SchemaUtils

import kotlinx.coroutines.runBlocking

import org.slf4j.event.Level

const val version = "PRX2"

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

    // Initialize database

    Database.init()

    // Create tables if they don't exist

    runBlocking { query {
        SchemaUtils.create (
            Articles,
            Articles.Content,
            Articles.Categories,
            Users,
            Comments,
            Users.UserInfo
        )
    }}

    // Initialize routes

    routing {
        route("/api") {
            articles()
            users()
            auth()
        }
    }

}
