package blogify.backend.resources.listings

import blogify.backend.resources.User
import blogify.backend.resources.models.Resource

data class ListingQuery<TResource : Resource> (
    val quantity: Int,
    val page: Int,
    val forUser: User?,
    val searchQuery: String?
)
