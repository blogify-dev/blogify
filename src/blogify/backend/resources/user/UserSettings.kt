package blogify.backend.resources.user

import blogify.backend.appContext
import blogify.backend.resources.static.models.StaticFile

import epgx.types.Jsonb

import io.ktor.http.ContentType

data class UserSettings (

    val profilePictureFile: StaticFile = StaticFile.None(ContentType.Image.Any),

    val coverPictureFile: StaticFile = StaticFile.None(ContentType.Image.Any)

) {
    companion object : Jsonb.Converter<UserSettings> by appContext.jsonbConverter()
}
