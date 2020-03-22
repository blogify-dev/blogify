package blogify.backend.persistence.postgres.orm.annotations

import blogify.backend.persistence.postgres.orm.models.PropertyMapping

@Target(AnnotationTarget.TYPE)
annotation class Cardinality(val cardinality: PropertyMapping.AssociativeMapping.Cardinality)
