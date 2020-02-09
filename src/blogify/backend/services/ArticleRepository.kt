package blogify.backend.services

import blogify.backend.database.Articles
import blogify.backend.persistence.sql.SqlRepository
import blogify.backend.resources.Article

object ArticleRepository : SqlRepository<Article>(table = Articles)
