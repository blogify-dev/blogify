package blogify.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

object TestDatabse {

    lateinit var instance: Database

    private fun configureHikariCP(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName        = "org.postgresql.Driver"
        config.jdbcUrl                = "jdbc:postgresql://localhost:5439/postgres"
        config.maximumPoolSize        = 24
        config.minimumIdle            = 6
        config.validationTimeout      = 10 * 1000
        config.connectionTimeout      = 10 * 1000
        config.maxLifetime            = 30 * 60 * 1000
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

}
