package blogify.backend.resources.static.models

sealed class StaticResourceHandle(val code: String) {

    class None(code: String): StaticResourceHandle(code)

    class Ok(code: String, val fileId: String): StaticResourceHandle(code)

}
