package me.benjozork.resources

import me.benjozork.resources.models.Resource

data class User(
    val name: String,
    val firebaseUid: String
) : Resource()