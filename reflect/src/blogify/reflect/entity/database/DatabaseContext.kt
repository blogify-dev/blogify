package blogify.reflect.entity.database

import blogify.common.util.MapCache
import blogify.reflect.entity.Entity
import blogify.reflect.entity.database.persistence.models.Repository

import kotlin.reflect.KClass

/**
 * Interface that gives database implementation context. Stores cached [repositories][Repository].
 *
 * @author Benjozork
 */
interface DatabaseContext {

    val repoCache: MapCache<KClass<out Entity>, Repository<Entity>>

}
