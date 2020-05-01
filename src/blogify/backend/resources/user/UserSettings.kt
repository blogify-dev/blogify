package blogify.backend.resources.user

import blogify.backend.resources.static.models.StaticFile
import blogify.reflect.models.Mapped

import io.ktor.http.ContentType

data class UserSettings (

    val profilePictureFile: StaticFile = StaticFile.None(ContentType.Image.Any),

    val coverPictureFile: StaticFile = StaticFile.None(ContentType.Image.Any)

) : Mapped()
