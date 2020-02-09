package blogify.backend.services

import blogify.backend.database.Comments
import blogify.backend.persistence.sql.SqlRepository
import blogify.backend.resources.Comment

object CommentRepository : SqlRepository<Comment>(table = Comments)
