package blogify.backend.bootstrap

import blogify.backend.blogifyMainModule
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.models.ResourceIdSerializer
import blogify.backend.search.models.Template
import blogify.backend.util.ContentTypeDeserializer
import blogify.backend.util.ContentTypeSerializer
import blogify.backend.util.InstantSerializer
import blogify.devend.blogifyDevModule
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI

import java.io.File
import java.security.KeyStore

import org.slf4j.LoggerFactory

@ExperimentalStdlibApi
private val objectMapper = jacksonObjectMapper().apply {
    val blogifyModule = SimpleModule()

    blogifyModule.addSerializer(ResourceIdSerializer)
    blogifyModule.addSerializer(Template.Field.Serializer)
    blogifyModule.addSerializer(ContentTypeSerializer)
    blogifyModule.addSerializer(InstantSerializer)

    blogifyModule.addDeserializer(ContentType::class.java, ContentTypeDeserializer)

    registerModule(blogifyModule)
}

@KtorExperimentalAPI
@ExperimentalStdlibApi
object BlogifyApplicationBootstrapper {

    private val logger = LoggerFactory.getLogger("blogify-application-bootstrapper")

    private const val version = "0.4.0-preRelease"

    @Suppress("GrazieInspection")
    private val asciiLogo = """
        __     __               _  ____      
       / /_   / /____   ____ _ (_)/ __/__  __
      / __ \ / // __ \ / __ `// // /_ / / / /
     / /_/ // // /_/ // /_/ // // __// /_/ / 
    /_.___//_/ \____/ \__, //_//_/   \__, /  
                     /____/         /____/   
    ---- Version $version - Development build -
    """.trimIndent()

    class StartConfiguration (
        val host: String,
        val port: Int,
        val tlsConfig: Tls? = null,
        val applicationContext: ApplicationContext = ApplicationContext(objectMapper),
        val isEnvDebug: Boolean = false
    ) {
        class Tls (
            val keyStorePath: String,
            val keyStorePassword: String,
            val keyAlias: String,
            val privateKeyPassword: String,
            val port: Int
        )

        val blogifyVersion: String = version
    }

    @JvmStatic fun main(args: Array<String>) {
        val configuration = doLoadConfiguration()

        doStartApplication(configuration)
    }

    private fun doLoadConfiguration(): StartConfiguration {
        val startHost = System.getenv("BLOGIFY_HOST")?.trim()
            ?: "0.0.0.0"

        val tlsEnabled = System.getenv("BLOGIFY_TLS_ENABLED")?.trim()
            ?.toIntOrNull()
            ?.takeIf { it in 0..1 }
            ?.let { it == 1 }
            ?: false

        val startPort = System.getenv("BLOGIFY_PORT")?.trim()
            ?.toIntOrNull()
            ?.takeIf { it in 0..65535 }
            ?: if (tlsEnabled) 443 else 80

        val debug = System.getenv("BLOGIFY_ENV_DEBUG")?.trim()
            ?.toIntOrNull()
            ?.takeIf { it in 0..1 }
            ?.let { it == 1 }
            ?: false

        val tlsConfig = if (tlsEnabled) { // Very temporary and insecure
            fun error(envVar: String): Nothing = kotlin.error("$envVar must be set and valid if BLOGIFY_ENABLE_TLS is set to 1")

            val tlsKeyStorePath = System.getenv("BLOGIFY_TLS_KEY_STORE")?.trim()
                ?: error("BLOGIFY_TLS_KEY_STORE")

            val tlsKeyStorePassword = System.getenv("BLOGIFY_TLS_KEY_STORE_PASSWORD")?.trim()
                ?: error("BLOGIFY_TLS_KEY_STORE_PASSWORD")

            val tlsKeyAlias = System.getenv("BLOGIFY_TLS_KEY_ALIAS")?.trim()
                ?: error("BLOGIFY_TLS_KEY_ALIAS")

            val tlsPrivateKeyPassword = System.getenv("BLOGIFY_TLS_PRIVATE_KEY_PASSWORD")?.trim()
                ?: error("BLOGIFY_TLS_PRIVATE_KEY_PASSWORD")

            val tlsPort = System.getenv("BLOGIFY_TLS_PORT")?.trim()
                ?.toIntOrNull()
                ?.takeIf { it in 0..65535 }
                ?: error("BLOGIFY_TLS_PORT")

            StartConfiguration.Tls (
                tlsKeyStorePath,
                tlsKeyStorePassword,
                tlsKeyAlias,
                tlsPrivateKeyPassword,
                tlsPort
            )
        } else null

        return StartConfiguration(
            host = startHost,
            port = startPort,
            tlsConfig = tlsConfig,
            isEnvDebug = debug
        )
    }

    private fun doStartApplication(configuration: StartConfiguration) {
        val server = embeddedServer (
            Netty, environment = applicationEngineEnvironment {
                // Main application module

                modules += { blogifyMainModule(configuration) }
                if (configuration.isEnvDebug)
                    modules += { blogifyDevModule(configuration) }

                // Default HTTP connector

                connector {
                    host = configuration.host
                    port = configuration.port
                }

                // HTTPS connector if needed

                if (configuration.tlsConfig != null) {
                    val tlsConfig = configuration.tlsConfig

                    val keyStore = KeyStore.getInstance("pcks12")
                    keyStore.load(File(tlsConfig.keyStorePath).inputStream(), tlsConfig.keyStorePassword.toCharArray())

                    sslConnector (
                        keyStore,
                        tlsConfig.keyAlias,
                        { tlsConfig.keyStorePassword.toCharArray() },
                        { tlsConfig.privateKeyPassword.toCharArray() },
                        builder = {
                            host = configuration.host
                            port = tlsConfig.port
                        }
                    )
                }
            }
        )

        println(asciiLogo)

        logger.info("Starting blogify embedded server ...")
        server.start(wait = true)
    }

}
