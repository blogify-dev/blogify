package blogify.backend.persistence.postgres.orm.models

enum class Cardinality {
    ONE_TO_ONE,
    ONE_TO_ONE_OR_NONE,
    MANY_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_MANY
}

enum class CollectionCardinality {
    MANY_TO_MANY,
    ONE_TO_MANY,
}
