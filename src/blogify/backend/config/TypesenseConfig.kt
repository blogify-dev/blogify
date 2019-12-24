package blogify.backend.config

import kotlinx.serialization.Serializable

@Serializable
data class TypesenseConfig(
    val host: String,
    val port: Int,
    val apiKey: String
)