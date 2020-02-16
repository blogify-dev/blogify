package blogify.backend.persistence.models

import blogify.backend.resources.models.Resource

import kotlin.reflect.KClass

abstract class DataStore (
    val configuration: Configuration
) {

    val name = configuration.name

    abstract fun <R : Resource> getRepository(klass: KClass<R>): Repository<R>

    open class Configuration {
        var name: String? = null
    }

}
