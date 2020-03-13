package blogify.backend.search.models

import blogify.backend.resources.models.Resource
import blogify.backend.search.Typesense
import blogify.backend.util.Dto

/**
 * Models for sending processed [Typesense] search results to a client
 *
 * @author Benjozork
 */
data class SearchView (
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
        val highlights: List<Search.Highlight>
    )

}
