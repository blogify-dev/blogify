package blogify.backend.database

import blogify.backend.config.Configs
import blogify.backend.config.DatabaseConfig
import blogify.backend.util.BException

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

/**
 * Meta object regrouping setup and utility functions for PostgreSQL.
 */
object DatabaseConnection {

    private val config = Configs.Database

    private fun configureHikariCP(dbConfig: DatabaseConfig): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName        = "org.postgresql.Driver"
        config.jdbcUrl                = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.databaseName}"
        config.maximumPoolSize        = 64
        config.minimumIdle            = 6
        config.validationTimeout      = 10 * 1000
        config.connectionTimeout      = 10 * 1000
        config.maxLifetime            = 30 * 60 * 1000
        config.leakDetectionThreshold = 60 * 1000
        config.isAutoCommit           = false
        config.transactionIsolation   = "TRANSACTION_REPEATABLE_READ"
        config.username               = dbConfig.username
        config.password               = dbConfig.password
        config.validate()
        return HikariDataSource(config)
    }

    fun connect() = Database.connect(configureHikariCP(config))

    open class Exception(causedBy: kotlin.Exception) : BException(causedBy)

}
