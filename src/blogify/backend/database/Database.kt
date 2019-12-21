package blogify.backend.database

import blogify.backend.util.BException

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

/**
 * Meta object regrouping setup and utility functions for PostgreSQL.
 */
object Database {

    lateinit var instance: Database

    private fun configureHikariCP(envDbHost: String, envDbPort: String, envDbUser: String, envDbPass: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName        = "org.postgresql.Driver"
        config.jdbcUrl                = "jdbc:postgresql://$envDbHost:$envDbPort/postgres"
        config.maximumPoolSize        = 24
        config.minimumIdle            = 6
        config.validationTimeout      = 10 * 1000
        config.connectionTimeout      = 10 * 1000
        config.maxLifetime            = 30 * 60 * 1000
        config.leakDetectionThreshold = 60 * 1000
        config.isAutoCommit           = false
        config.transactionIsolation   = "TRANSACTION_REPEATABLE_READ"
        config.username               = envDbUser
        config.password               = envDbPass
        config.validate()
        return HikariDataSource(config)
    }

    fun init() {

        // Temporary

        val envDbHost = System.getenv("BLOGIFY_DB_HOST").takeIf { it?.isNotBlank() ?: false } ?: "db"
        val envDbPort = System.getenv("BLOGIFY_DB_PORT").takeIf { it?.isNotBlank() ?: false } ?: "5432"
        val envDbUser = System.getenv("BLOGIFY_DB_USER").takeIf { it?.isNotBlank() ?: false } ?: "postgres"
        val envDbPass = System.getenv("BLOGIFY_DB_PASS").takeIf { it?.isNotBlank() ?: false } ?: ""

        instance = Database.connect(configureHikariCP(envDbHost, envDbPort, envDbUser, envDbPass))
    }

    open class Exception(causedBy: kotlin.Exception) : BException(causedBy) {

        class NotFound(causedBy: BException) : Exception(causedBy)

        class MultipleFound(causedBy: BException) : Exception(causedBy)

    }

}