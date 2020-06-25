package blogify.backend.search.models

import blogify.backend.entity.Resource
import blogify.backend.search.Typesense
import reflectr.util.Dto

/**
 * Models for deserializing JSON returned by [Typesense]
 *
 * @author hamza1311
 */
@ExperimentalStdlibApi
data class Search<R : Resource> (
    val facet_counts: List<Any>?, // |\
    val found: Int?,              // | Will not appear on no results
    val hits: List<Hit>?,         // |/
    val page: Int,
    val search_time_ms: Int
) {

    /**
     * Represents a hit
     */
    data class Hit (
        val document: Dto,
        val highlights: List<Highlight>
    )

    data class Highlight (
        val field: String,
        val snippet: String
    )

}
