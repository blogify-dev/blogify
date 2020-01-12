package blogify.backend.services

import blogify.backend.database.Users
import blogify.backend.resources.User
import blogify.backend.services.models.Service

object UserService : Service<User>(table = Users)
