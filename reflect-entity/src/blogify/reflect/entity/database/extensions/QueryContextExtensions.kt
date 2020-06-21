package blogify.reflect.entity.database.extensions

import blogify.common.util.assertGet
import blogify.reflect.entity.Entity
import blogify.reflect.entity.database.DatabaseContext
import blogify.reflect.entity.database.QueryContext
import blogify.reflect.entity.database.annotations.table
import blogify.reflect.entity.database.persistence.models.Repository
import blogify.reflect.entity.database.persistence.postgres.PostgresRepository

import kotlin.reflect.KClass

/**
 * Provides a repository for [TEntity]
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified TEntity : Entity> DatabaseContext.repository(): Repository<TEntity> =
    this.repoCache.findOr(TEntity::class) {
        PostgresRepository(TEntity::class.table) as Repository<Entity>
    }.get() as Repository<TEntity>

/**
 * Provides a repository for [TEntity]
 */
@Suppress("UNCHECKED_CAST")
fun <TEntity : Entity> DatabaseContext.repository(klass: KClass<out TEntity>): Repository<TEntity> =
    this.repoCache.findOr(klass) {
        PostgresRepository(klass.table) as Repository<Entity>
    }.get() as Repository<TEntity>

/**
 * Provides a repository for [TEntity]
 */
inline fun <reified TEntity : Entity> QueryContext.repository(): Repository<TEntity> =
    databaseContext.repository()

/**
 * Provides a repository for [TEntity]
 */
inline fun <reified TEntity : Entity> QueryContext.repository(klass: KClass<out TEntity>): Repository<TEntity> =
    databaseContext.repository(klass)
