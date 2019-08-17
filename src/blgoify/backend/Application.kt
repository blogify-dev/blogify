package blgoify.backend

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.auth.*
import io.ktor.jackson.*
import io.ktor.features.*

import com.fasterxml.jackson.databind.*

import blgoify.backend.routes.articles.articles
import blgoify.backend.routes.articles.articleComments
import blgoify.backend.routes.users
import blgoify.backend.database.Database
import blgoify.backend.database.Articles

import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

import org.slf4j.event.Level
import java.util.UUID

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args).also {
    println(Articles.selectAll().map { it[Articles.title] })
}

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

    Database.init()

    transaction {
        addLogger(Slf4jSqlDebugLogger)

        create(Articles)

        Articles.insert {
            it[uuid]      = UUID.randomUUID()
            it[title]     = "Lucy should push her fucking code"
            it[createdAt] = System.currentTimeMillis() * 1000L
        }

        println(Articles.selectAll())
    }

    routing {
        route("/api") {
            articles()
            users()
            articleComments()
        }
    }
}