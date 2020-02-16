package blogify.backend.search.ext

import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.sanitize
import blogify.backend.search.models.Search
import blogify.backend.search.models.SearchView
import blogify.backend.services.models.Repository
import blogify.backend.util.Sr
import blogify.backend.util.toUUID

import java.lang.Exception
import java.lang.IllegalStateException

suspend inline fun <reified T : Resource> Search.Hit.fetchResource(repository: Repository<T>): Sr<T> {
    val resourceUUID = (this.document["uuid"] as String).toUUID()
    return repository.get(Resource.ObjectResolver.FakeApplicationCall, resourceUUID)
}

suspend inline fun <reified R: Resource> Search<R>.asSearchView(requestContext: RequestContext): SearchView {
    val processedHits = try {
        this.hits?.map {
            SearchView.Hit(it.fetchResource<R>(requestContext.repository<R>()).get().sanitize(), it.highlights)
        } ?: emptyList()
    } catch (e: Exception) {
        throw IllegalStateException("error while parsing search results: ${e::class.simpleName}: ${e.message}", e)
    }

    return SearchView(this.facet_counts, this.found, processedHits, this.page, this.search_time_ms)
}
