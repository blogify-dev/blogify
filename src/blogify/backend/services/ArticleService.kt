package blogify.backend.services

import blogify.backend.database.Articles
import blogify.backend.resources.Article
import blogify.backend.services.models.Service

object ArticleService : Service<Article>(table = Articles)
