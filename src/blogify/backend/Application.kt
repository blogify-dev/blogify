package blogify.backend

import com.fasterxml.jackson.databind.module.SimpleModule

import com.andreapivetta.kolor.cyan

import com.fasterxml.jackson.databind.*

import blogify.backend.config.Configs
import blogify.backend.routing.makeArticleRoutes
import blogify.backend.routing.users.users
import blogify.backend.database.Database
import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.database.ImageUploadablesMetadata
import blogify.backend.database.Uploadables
import blogify.backend.database.Users
import blogify.backend.routing.auth
import blogify.backend.database.handling.query
import blogify.backend.persistence.postgres.PostgresDataStore
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.routing.admin.adminSearch
import blogify.backend.routing.static
import blogify.backend.search.Typesense
import blogify.backend.search.ext._searchTemplate
import blogify.backend.search.models.Template
import blogify.backend.util.ContentTypeSerializer
import blogify.backend.util.SinglePageApplication
import blogify.backend.util.matches

import io.ktor.application.call
import io.ktor.features.Compression
import io.ktor.features.GzipEncoder
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CachingHeaders
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.HttpsRedirect
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing

import org.jetbrains.exposed.sql.SchemaUtils

import kotlinx.coroutines.runBlocking

import org.slf4j.event.Level

private const val version = "0.3.0-preRelease"

@Suppress("GrazieInspection")
private const val asciiLogo = """
    __     __               _  ____      
   / /_   / /____   ____ _ (_)/ __/__  __
  / __ \ / // __ \ / __ `// // /_ / / / /
 / /_/ // // /_/ // /_/ // // __// /_/ / 
/_.___//_/ \____/ \__, //_//_/   \__, /  
                 /____/         /____/   
---- Version $version - Development build -
"""

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.mainModule(@Suppress("UNUSED_PARAMETER") testing: Boolean = false) {

    // Print startup logo

    println(asciiLogo.cyan())

    // Initialize jackson

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)

            // Register a serializer for Resource and Type.
            // This will only affect pure Resource objects, so elements produced by the slicer are not affected,
            // since those don't use Jackson for root serialization.

            val blogifyModule = SimpleModule()
            blogifyModule.addSerializer(Resource.ResourceIdSerializer)
            blogifyModule.addSerializer(Template.Field.Serializer)
            blogifyModule.addSerializer(ContentTypeSerializer)

            registerModule(blogifyModule)
        }
    }

    // Initialize HTTPS refirection

    install(HttpsRedirect) {
        // The port to redirect to. By default 443, the default HTTPS port.
        sslPort = 443
        // 301 Moved Permanently, or 302 Found redirect.
        permanentRedirect = true
    }

    // Initialize call logging

    install(CallLogging) {
        level = Level.TRACE
    }

    // Redirect every unknown route to SPA

    install(SinglePageApplication) {
        folderPath = "/frontend"
    }

    // Compression

    install(Compression) {
        encoder("gzip0", GzipEncoder)
    }

    // Default headers

    install(DefaultHeaders) {
        header("X-Blogify-Version", "blogify-core $version")
        header("X-Blogify-Backend", "Ktor 1.3.1")
    }

    // Caching headers

    install(CachingHeaders) {
        options {
            val contentType = it.contentType?.withoutParameters() ?: return@options null

            when {
                contentType matches ContentType.Application.JavaScript ->
                    CachingOptions(CacheControl.MaxAge(30 * 60))
                contentType matches ContentType.Image.Any ->
                    CachingOptions(CacheControl.MaxAge(60 * 60))
                contentType matches ContentType.Application.Json ->
                    CachingOptions(CacheControl.MaxAge(60))
                else -> null
            }
        }
    }

    // Initialize database

    Database.init()

    // Create tables if they don't exist

    runBlocking {
        query {
            SchemaUtils.createMissingTablesAndColumns (
                Articles,
                Articles.Categories,
                Users,
                Users.Follows,
                Articles.Likes,
                Comments,
                Comments.Likes,
                Uploadables,
                ImageUploadablesMetadata
            )
        }

        // Submit search templates

        Typesense.submitResourceTemplate(Article::class._searchTemplate)
        Typesense.submitResourceTemplate(User::class._searchTemplate)

    }

    // Create an application context

    val dataStore = PostgresDataStore {

        val config = Configs.Database

        host = config.host
        port = config.port

        username = config.username
        password = config.password

        database = config.databaseName

    }

    val appContext = ApplicationContext(dataStore)

    // Initialize routes

    routing {

        route("/api") {
            makeArticleRoutes(appContext)
            users(appContext)
            auth(appContext)
            static(appContext)
            adminSearch(appContext)
        }

        get("/") {
            call.respondRedirect("/home")
        }

    }

}
