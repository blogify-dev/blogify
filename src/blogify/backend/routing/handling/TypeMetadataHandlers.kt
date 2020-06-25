package blogify.backend.routing.handling

import reflectr.entity.Entity
import reflectr.entity.metadata.entity
import reflectr.analysis.extensions.descriptor
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
