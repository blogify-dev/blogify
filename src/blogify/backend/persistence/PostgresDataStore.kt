package blogify.backend.persistence

import blogify.backend.annotations.table
import blogify.backend.config.Configs
import blogify.backend.persistence.models.DataStore
import blogify.backend.persistence.sql.SqlRepository
import blogify.backend.resources.models.Resource
import blogify.backend.services.models.Repository
import blogify.backend.util.MapCache

import kotlin.reflect.KClass

class PostgresDataStore (
    configure: PostgresConfiguration.() -> Unit
) : DataStore(PostgresConfiguration().apply(configure)) {

    val config = configure(PostgresConfiguration())

    val repoCache = MapCache<KClass<*>, Repository<*>>()

    override fun <R : Resource> getRepository(klass: KClass<R>): Repository<R> {
        @Suppress("UNCHECKED_CAST")
        return repoCache.getOrElse(klass) { SqlRepository(klass.table) } as Repository<R>
    }

    class PostgresConfiguration : DataStore.Configuration() {

        var host: String? = null
        var port: Int? = null

        var username: String? = null
        var password: String? = null

        var database: String? = null

    }

}