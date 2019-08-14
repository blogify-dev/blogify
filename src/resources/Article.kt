package me.benjozork.resources

import me.benjozork.resources.models.Resource
import java.util.*

data class Article(
    val title: String,
    val createdAt: Long = Date().time
) : Resource()
