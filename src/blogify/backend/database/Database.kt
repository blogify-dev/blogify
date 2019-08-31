package blogify.backend.database

import blogify.backend.util.BException

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

object Database {

    lateinit var instance: Database

    private fun configureHikariCP(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName        = "org.postgresql.Driver"
        config.jdbcUrl                = "jdbc:postgresql://db:5432/postgres"
        config.maximumPoolSize        = 24
        config.minimumIdle            = 6
        config.validationTimeout      = 10 * 1000
        config.connectionTimeout      = 10 * 1000
        config.maxLifetime            = 30 * 1000
        config.leakDetectionThreshold = 60 * 1000
        config.isAutoCommit           = false
        config.transactionIsolation   = "TRANSACTION_REPEATABLE_READ"
        config.username               = "postgres"
        config.password               = ""
        config.validate()
        return HikariDataSource(config)
    }

    fun init() {
        instance = Database.connect(configureHikariCP())
    }

    open class Exception(causedBy: kotlin.Exception) : BException(causedBy) {

        class NotFound(causedBy: BException) : Exception(causedBy)

        class MultipleFound(causedBy: BException) : Exception(causedBy)

    }

}