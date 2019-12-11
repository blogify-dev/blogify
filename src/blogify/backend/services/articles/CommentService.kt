package blogify.backend.services.articles

import blogify.backend.database.Comments
import blogify.backend.resources.Comment
import blogify.backend.services.models.Service

object CommentService : Service<Comment>(table = Comments)
