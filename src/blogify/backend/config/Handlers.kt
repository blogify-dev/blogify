package blogify.backend.config

import com.charleskorn.kaml.Yaml
import java.io.File

fun getDatabaseConfig() = Yaml.default.parse(DatabaseConfig.serializer(), File("./db.yaml").readText())

fun getTypesenseConfig() = Yaml.default.parse(TypesenseConfig.serializer(), File("./ts.yaml").readText())