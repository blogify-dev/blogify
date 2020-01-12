package blogify.backend.services

import blogify.backend.database.Comments
import blogify.backend.resources.Comment
import blogify.backend.services.models.Service

object CommentService : Service<Comment>(table = Comments)
