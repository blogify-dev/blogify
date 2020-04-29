package blogify.backend

import blogify.backend.bootstrap.BlogifyApplicationBootstrapper

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.*

import blogify.backend.config.Configs
import blogify.backend.database.*
import blogify.backend.routing.makeArticleRoutes
import blogify.backend.routing.users.makeUserRoutes
import blogify.backend.routing.makeAuthRoutes
import blogify.backend.database.handling.query
import blogify.backend.database.tables.*
import blogify.backend.persistence.postgres.PostgresDataStore
import blogify.backend.pipelines.GenericCallPipeline
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.routing.admin.makeAdminRoutes
import blogify.backend.routing.makePushServerRoutes
import blogify.backend.routing.makeStaticRoutes
import blogify.backend.search.Typesense
import blogify.backend.search.ext._searchTemplate
import blogify.backend.search.models.Template
import blogify.backend.util.ContentTypeSerializer
import blogify.backend.util.InstantSerializer
import blogify.backend.util.SinglePageApplication
import blogify.backend.util.matches

import io.ktor.application.call
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.WebSockets

import org.jetbrains.exposed.sql.SchemaUtils

import kotlinx.coroutines.runBlocking

import org.slf4j.event.Level

private val dataStore = PostgresDataStore {

    val config = Configs.Database

    host = config.host
    port = config.port

    username = config.username
    password = config.password

    database = config.databaseName

}

val appContext = ApplicationContext(dataStore)
@property:Suppress("unused")
val GenericCallPipeline.applicationContext
    get() = appContext

@KtorExperimentalAPI
fun Application.blogifyMainModule(configuration: BlogifyApplicationBootstrapper.StartConfiguration) {

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
            blogifyModule.addSerializer(InstantSerializer)

            registerModule(blogifyModule)
        }
    }

    // Initialize HTTPS refirection if TLS is enabled

    if (configuration.tlsConfig != null) {
        install(HttpsRedirect) {
            sslPort = configuration.tlsConfig.port
            permanentRedirect = true
        }
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
        header("X-Blogify-Version", "blogify-core ${configuration.blogifyVersion}")
        header("X-Blogify-Backend", "Ktor 1.3.2")
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

    // WebSockets

    install(WebSockets) {
        timeoutMillis = 30_000
        pingPeriodMillis = 15_000
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
                ImageUploadablesMetadata,
                Events
            )
        }

        // Submit search templates

        Typesense.submitResourceTemplate(Article::class._searchTemplate)
        Typesense.submitResourceTemplate(User::class._searchTemplate)

    }

    // Initialize routes

    routing {

        route("/api") {
            makeArticleRoutes(appContext)
            makeUserRoutes(appContext)
            makeAuthRoutes(appContext)
            makeStaticRoutes(appContext)
            makeAdminRoutes(appContext)
        }

        makePushServerRoutes()

        get("/") {
            call.respondRedirect("/home")
        }

    }

}
