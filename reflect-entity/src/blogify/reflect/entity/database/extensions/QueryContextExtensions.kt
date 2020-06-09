package blogify.reflect.entity.database.extensions

import blogify.common.util.assertGet
import blogify.reflect.entity.Entity
import blogify.reflect.entity.database.DatabaseContext
import blogify.reflect.entity.database.QueryContext
import blogify.reflect.entity.database.annotations.table
import blogify.reflect.entity.database.persistence.models.Repository
import blogify.reflect.entity.database.persistence.postgres.PostgresRepository

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
inline fun <reified TResource : Entity> DatabaseContext.repository(): Repository<TResource> =
    this.repoCache.findOr(TResource::class) {
        PostgresRepository(TResource::class.table) as Repository<Entity>
    }.assertGet() as Repository<TResource>

@Suppress("UNCHECKED_CAST")
fun <TResource : Entity> DatabaseContext.repository(klass: KClass<out TResource>): Repository<TResource> =
    this.repoCache.findOr(klass) {
        PostgresRepository(klass.table) as Repository<Entity>
    }.assertGet() as Repository<TResource>

inline fun <reified TEntity : Entity> QueryContext.repository(): Repository<TEntity> =
    databaseContext.repository()

inline fun <reified TEntity : Entity> QueryContext.repository(klass: KClass<out TEntity>): Repository<TEntity> =
    databaseContext.repository(klass)
