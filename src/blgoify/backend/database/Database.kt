package blgoify.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

object Database {

    lateinit var instance: Database

    private fun configureHikariCP(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName      = "org.postgresql.Driver"
        config.jdbcUrl              = "jdbc:postgresql://db:5432/postgres"
        config.maximumPoolSize      = 3
        config.isAutoCommit         = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.username             = "postgres"
        config.password             = ""
        config.validate()
        return HikariDataSource(config)
    }

    fun init() {
        instance = Database.connect(configureHikariCP())
    }

}