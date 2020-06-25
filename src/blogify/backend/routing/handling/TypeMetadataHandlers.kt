package blogify.backend.routing.handling

import reflectify.entity.Entity
import reflectify.entity.metadata.entity
import reflectify.analysis.extensions.descriptor
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.pipelines.wrapping.RequestContextFunction

import io.ktor.response.respond

import kotlin.reflect.KClass

@BlogifyDsl
@ExperimentalStdlibApi
fun <TEntity : Entity> getTypeMetadata(klass: KClass<TEntity>): RequestContextFunction<Unit> = {
    call.respond(object {
        val entity = klass.descriptor.entity
        val properties = klass.descriptor.propertyDescriptors.map { it.key.name to it.value.entity }.toMap()
    })
}
