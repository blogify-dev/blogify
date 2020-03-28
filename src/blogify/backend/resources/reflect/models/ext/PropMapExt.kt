package blogify.backend.resources.reflect.models.ext

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.util.filterThenMapValues

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

import com.andreapivetta.kolor.red

inline fun <reified TMapped : Mapped> KProperty1<TMapped, *>.handle() =
    TMapped::class.cachedPropMap().map.values.first { it.name == this.name }

inline fun <reified TMapped : Mapped> KProperty1<TMapped, *>.okHandle() =
    TMapped::class.cachedPropMap().ok().values.first { it.name == this.name }

inline val <TResource : Resource> KClass<TResource>.uuidHandle get() =
    this.cachedPropMap().ok().values.firstOrNull { it.name == "uuid" } ?: error("fatal: no uuid handle on class '${this.simpleName}'. Are you sure it's a Resource ?".red())

/**
 * Only returns [PropMap.PropertyHandle]s that respect interface [PropMap.PropertyHandle.Valid]
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> PropMap<TMapped>.valid() = this.map
    .filterThenMapValues (
        { it is PropMap.PropertyHandle.Valid },
        { it.value as PropMap.PropertyHandle.Valid }
    )

/**
 * Only returns [PropMap.PropertyHandle]s that are of type [PropMap.PropertyHandle.Ok]
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> PropMap<TMapped>.ok() = this.map
    .filterThenMapValues (
        { it is PropMap.PropertyHandle.Ok },
        { it.value as PropMap.PropertyHandle.Ok }
    )

/**
 * Only returns [PropMap.PropertyHandle]s that are of type [PropMap.PropertyHandle.AccessDenied]
 *
 * @author Benjozork
 */
fun <TMapped : Mapped> PropMap<TMapped>.accessDenied() = this.map.filterThenMapValues ({ it is PropMap.PropertyHandle.AccessDenied }, { it.value as PropMap.PropertyHandle.AccessDenied })
