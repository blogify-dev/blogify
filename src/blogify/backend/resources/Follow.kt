package blogify.backend.resources

import blogify.backend.resources.user.User

data class Follow(
    val following: User,
    val follower: User
)
