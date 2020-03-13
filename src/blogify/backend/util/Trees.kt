package blogify.backend.util

import blogify.backend.database.Comments
import blogify.backend.resources.Comment
import blogify.backend.resources.reflect.sanitize
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.models.Resource.ObjectResolver.FakeRequestContext

import io.ktor.application.ApplicationCall

import com.andreapivetta.kolor.lightMagenta
import com.andreapivetta.kolor.yellow

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("blogify-comment-tree")

/**
 * Recursively expands a [Comment] node to the specified [depth]
 *
 * @param request the context of the [call][ApplicationCall] resulting in this operation,
 *                    used for caching purposes. Defaults to [FakeApplicationCall] without caching.
 *
 * @param rootNode    the root node to do expansion on. Stays constant in subsequent recursive calls to provide logging convenience.
 * @param currentNode the current node the function is operating on. Defaults to [rootNode] unless used by a recursive call.
 *                    **WARNING : DO NOT SET MANUALLY !**
 *
 * @param depth the depth to expand to
 *
 * @return the expanded tree
 *
 * @author Benjozork
 */
suspend fun expandCommentNode(request: RequestContext = FakeRequestContext, repository: Repository<Comment>, rootNode: Comment, currentNode: Comment = rootNode, depth: Int): Dto {
    val sanitizedNode = currentNode.sanitize().toMutableMap()

    logger.debug("expanding tree node - root: ${rootNode.uuid.short()}, current: ${currentNode.uuid.short()}, depth: $depth".lightMagenta())

    if (depth == 0) {
        return sanitizedNode
    } else {
        val nodeDirectChildren = repository.getMatching(request) { Comments.parentComment eq currentNode.uuid }
            .fold (
                success = { it },
                failure = { error("error during node expand") }
            )

        if (nodeDirectChildren.isEmpty())
            logger.debug("no children for ${currentNode.uuid.short()}".yellow())
        else
            logger.debug("${nodeDirectChildren.size} children for ${currentNode.uuid.short()}".yellow())

        sanitizedNode["children"] = nodeDirectChildren.map { expandCommentNode(request, repository, rootNode, it, depth - 1) }
    }

    return sanitizedNode
}
