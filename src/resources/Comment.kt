package me.benjozork.resources

import me.benjozork.resources.models.Resource
import java.util.*

data class Comment(
    val commenter: UUID,
    val article: UUID,
    val content: String
) : Resource()