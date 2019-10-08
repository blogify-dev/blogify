package blogify.backend.util

import blogify.backend.database.Comments
import blogify.backend.resources.Comment
import blogify.backend.resources.models.Resource.ObjectResolver.FakeApplicationCall
import blogify.backend.resources.slicing.sanitize
import blogify.backend.services.articles.CommentService

import io.ktor.application.ApplicationCall

import com.andreapivetta.kolor.lightMagenta
import com.andreapivetta.kolor.yellow

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("blogify-comment-tree")

/**
 * Recursively expands a [Comment] node to the specified [depth]
 *
 * @param callContext the context of the [call][ApplicationCall] resulting in this operation,
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
suspend fun expandCommentNode(callContext: ApplicationCall = FakeApplicationCall, rootNode: Comment, currentNode: Comment = rootNode, depth: Int): Map<String, Any?> {
    val sanitizedNode = currentNode.sanitize().toMutableMap()

    logger.debug("expanding tree currentNode - root: ${rootNode.uuid.short()}, current: ${currentNode.uuid.short()}, depth: $depth".lightMagenta())

    if (depth == 0) {
        return sanitizedNode
    } else {
        val nodeDirectChildren = CommentService.getMatching(callContext) { Comments.parentComment eq currentNode.uuid }
            .fold (
                success = { it },
                failure = { error("error during node expand") }
            )

        if (nodeDirectChildren.isEmpty())
            logger.debug("no children for ${currentNode.uuid.short()}".yellow())
        else
            logger.debug("${nodeDirectChildren.size} children for ${currentNode.uuid.short()}".yellow())

        sanitizedNode["children"] = nodeDirectChildren.map { expandCommentNode(callContext, rootNode, it, depth - 1) }
    }

    return sanitizedNode
}