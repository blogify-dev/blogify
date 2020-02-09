package blogify.backend.pipelines

import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

/**
 * Context object that wraps an [ApplicationCall] and server execution context information,
 * instead of exposing the whole [PipelineContext].
 *
 * @author Benjozork
 */
class RequestContext (
    val applicationContext: ApplicationContext,
    val call: ApplicationCall
) {

    suspend fun execute(function: RequestContextFunction<Any?>, subject: Any?) {
        function(this, subject)
    }

}

typealias RequestContextFunction<TSubject> = suspend RequestContext.(TSubject) -> Unit
