package blogify.backend.database.persistence.postgres

import blogify.backend.annotations.table
import blogify.backend.database.persistence.models.DataStore
import blogify.backend.database.persistence.models.Repository
import blogify.backend.util.MapCache
import blogify.reflect.entity.Entity

import kotlin.reflect.KClass

class PostgresDataStore (
    configure: PostgresConfiguration.() -> Unit
) : DataStore(
    PostgresConfiguration().apply(configure)) {

    val config = PostgresConfiguration()
        .apply(configure)

    val repoCache = MapCache<KClass<*>, Repository<*>>()

    override fun <R : Entity> getRepository(klass: KClass<R>): Repository<R> {
        @Suppress("UNCHECKED_CAST")
        return repoCache.findOr(klass) {
            PostgresRepository(klass.table)
        }.get()
    }

    class PostgresConfiguration : DataStore.Configuration() {

        var host: String? = null
        var port: Int? = null

        var username: String? = null
        var password: String? = null

        var database: String? = null

    }

}
