package blogify.backend.resources.user

import reflectify.models.Mapped
import blogify.backend.appContext
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.resources.user.annotations.MaxFileSize

import epgx.types.JsonbColumnType

import io.ktor.http.ContentType

@ExperimentalStdlibApi
data class UserSettings (

    @MaxFileSize(5.0)
    val profilePictureFile: StaticFile = StaticFile.None(ContentType.Image.Any),

    @MaxFileSize(10.0)
    val coverPictureFile: StaticFile = StaticFile.None(ContentType.Image.Any)

) : Mapped() {
    companion object : JsonbColumnType.Converter<UserSettings> by appContext.createJsonbConverter()
}
