package blogify.backend.persistence.postgres.orm.annotations

import blogify.backend.persistence.postgres.orm.models.CollectionCardinality

@Target(AnnotationTarget.TYPE)
annotation class Cardinality(val cardinality: CollectionCardinality)
