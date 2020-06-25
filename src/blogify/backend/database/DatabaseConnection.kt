package blogify.backend.database

import blogify.backend.appContext
import blogify.backend.config.DatabaseConfig

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

/**
 * Meta object regrouping setup and utility functions for PostgreSQL.
 */
@ExperimentalStdlibApi
object DatabaseConnection {

    private fun configureHikariCP(dbConfig: DatabaseConfig): HikariDataSource =
        HikariDataSource(HikariConfig().apply {
            driverClassName        = "org.postgresql.Driver"
            jdbcUrl                = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.databaseName}"
            maximumPoolSize        = 64
            minimumIdle            = 6
            validationTimeout      = 10 * 1000
            connectionTimeout      = 10 * 1000
            maxLifetime            = 30 * 60 * 1000
            leakDetectionThreshold = 60 * 1000
            isAutoCommit           = false
            transactionIsolation   = "TRANSACTION_REPEATABLE_READ"
            username               = dbConfig.username
            password               = dbConfig.password
            validate()
        })


    fun connect(config: DatabaseConfig = appContext.databaseConfig) = Database.connect(configureHikariCP(config))

}
