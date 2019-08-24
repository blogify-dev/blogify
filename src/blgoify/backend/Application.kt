package blgoify.backend

import com.fasterxml.jackson.databind.SerializationFeature

import com.andreapivetta.kolor.cyan

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing

import blgoify.backend.database.Articles
import blgoify.backend.database.Comments
import blgoify.backend.database.Database
import blgoify.backend.database.Users
import blgoify.backend.routes.articles.articles
import blgoify.backend.routes.users
import blgoify.backend.util.query

import org.jetbrains.exposed.sql.SchemaUtils

import org.slf4j.event.Level

import kotlinx.coroutines.runBlocking



const val version = "PRX0"

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

    println(asciiLogo.cyan())

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
        }
    }
}
