package blogify.backend.search.ext

import blogify.common.util.toUUID
import blogify.common.util.Sr
import reflectr.sanitize
import blogify.database.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.entity.Resource
import blogify.backend.search.models.Search
import blogify.backend.search.models.SearchView

import java.lang.Exception
import java.lang.IllegalStateException

@ExperimentalStdlibApi
suspend inline fun <reified T : Resource> Search.Hit.fetchResource(requestContext: RequestContext, repository: Repository<out T>): Sr<T> {
    val resourceUUID = (this.document["uuid"] as String).toUUID()
    return repository.get(requestContext, resourceUUID)
}

@ExperimentalStdlibApi
suspend inline fun <reified R: Resource> Search<R>.asSearchView(requestContext: RequestContext): SearchView {
    val processedHits = try {
        this.hits?.map {
            SearchView.Hit(it.fetchResource(requestContext, requestContext.repository<R>()).get().sanitize(), it.highlights)
        } ?: emptyList()
    } catch (e: Exception) {
        throw IllegalStateException("error while parsing search results: ${e::class.simpleName}: ${e.message}", e)
    }

    return SearchView(this.facet_counts, this.found, processedHits, this.page, this.search_time_ms)
}
