package blogify.backend.resources.search

import blogify.backend.resources.models.Resource

data class Search<S : Resource>(
    val _shards: Shards,
    val hits: Hits<S>,
    val timed_out: Boolean,
    val took: Int
) {
    data class Shards(
        val failed: Int,
        val skipped: Int,
        val successful: Int,
        val total: Int
    )

    data class Hits<S : Resource>(
        val hits: List<Hit<S>>,
        val max_score: Double,
        val total: Total
    )

    data class Hit<S : Resource>(
        val _id: String,
        val _index: String,
        val _score: Double,
        val _source: S,
        val _type: String
    )

    data class Total(
        val relation: String,
        val value: Int
    )

}