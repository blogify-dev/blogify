package blogify.backend.persistence.postgres.orm.query.extensions

import blogify.backend.persistence.postgres.orm.query.models.Pointer
import blogify.backend.resources.models.Resource

import kotlin.reflect.KProperty1

infix fun <TRoot : Resource, TContainer : Resource, TValue : Resource, TNewValue : Any>
        Pointer<TRoot, TContainer, TValue>.then(next: KProperty1<TValue, TNewValue>) =
    Pointer(this, next)
