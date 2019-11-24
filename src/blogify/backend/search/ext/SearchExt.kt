package blogify.backend.search.ext

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.sanitize
import blogify.backend.search.models.Search
import blogify.backend.search.models.SearchView
import blogify.backend.services.models.ResourceResult
import blogify.backend.util.service
import blogify.backend.util.toUUID

import java.lang.Exception
import java.lang.IllegalStateException

suspend inline fun <reified T : Resource> Search.Hit.fetchResource(): ResourceResult<T> {
    val resourceUUID = (this.document["uuid"] as String).toUUID()
    return T::class.service.get(Resource.ObjectResolver.FakeApplicationCall, resourceUUID)
}

suspend inline fun <reified R: Resource> Search<R>.asSearchView(): SearchView {
    val processedHits = try {
        this.hits?.map {
            SearchView.Hit(it.fetchResource<R>().get().sanitize(), it.highlights)
        } ?: emptyList()
    } catch (e: Exception) {
        throw IllegalStateException("error while parsing search results: ${e::class.simpleName}: ${e.message}", e)
    }

    return SearchView(this.facet_counts, this.found, processedHits, this.page, this.search_time_ms)
}
