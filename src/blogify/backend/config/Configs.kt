package blogify.backend.config

object Configs {

    val Database = loadConfig("db", DatabaseConfig.serializer())

    val Typesense = loadConfig("ts", TypesenseConfig.serializer())

}
