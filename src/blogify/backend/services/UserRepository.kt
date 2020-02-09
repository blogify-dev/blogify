package blogify.backend.services

import blogify.backend.database.Users
import blogify.backend.persistence.sql.SqlRepository
import blogify.backend.resources.User

object UserRepository : SqlRepository<User>(table = Users)
