package blogify.backend.routing.handling

import blogify.reflect.entity.Entity
import blogify.reflect.entity.metadata.entity
import blogify.reflect.analysis.extensions.descriptor
import blogify.filtering.metadata.filtering
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.pipelines.wrapping.RequestContextFunction

import io.ktor.response.respond

import kotlin.reflect.KClass

@BlogifyDsl
fun <TEntity : Entity> getTypeMetadata(klass: KClass<TEntity>): RequestContextFunction<Unit> = {
    call.respond(object {
        val entity = klass.descriptor.entity
        val properties = klass.descriptor.propertyDescriptors.map {
            it.key.name to mapOf (
                "entity" to it.value.entity,
                "filtering" to it.value.filtering
            )
        }.toMap()
    })
}
