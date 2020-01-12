package blogify.backend.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig (
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val databaseName: String
)
