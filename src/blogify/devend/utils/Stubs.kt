package blogify.devend.utils

import blogify.backend.resources.Article
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.resources.user.User
import blogify.backend.util.hash
import blogify.common.util.toUUID
import io.ktor.http.ContentType

val user = User(
    username = "test_user",
    password = "test_pass".hash(),
    name = "test_name",
    email = "test@fuck.com",
    profilePicture = StaticFile.None(ContentType.Any),
    coverPicture = StaticFile.None(ContentType.Any),
    uuid = "3aee057d-e6e1-4b13-a0a7-0ea8f1f841f5".toUUID()
)

val article = Article(
    title = "The title of article",
    createdBy = user,
    content = "Content of content. should be a lot of text",
    summary = "Article's summary, short little summary",
    categories = listOf(Article.Category("category 1"), Article.Category("category 2"))
)
