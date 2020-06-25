package blogify.backend.resources

import blogify.backend.resources.user.User

@ExperimentalStdlibApi
data class Follow(
    val following: User,
    val follower: User
)
