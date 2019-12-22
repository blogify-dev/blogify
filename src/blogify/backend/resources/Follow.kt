package blogify.backend.resources

data class Follow(
    val following: User,
    val follower: User
)
