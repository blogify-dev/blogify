package blogify.backend.database.persistence.models

import blogify.reflect.entity.Entity

import kotlin.reflect.KClass

abstract class DataStore (
    val configuration: Configuration
) {

    val name = configuration.name

    abstract fun <R : Entity> getRepository(klass: KClass<R>): Repository<R>

    open class Configuration {
        var name: String? = null
    }

}
