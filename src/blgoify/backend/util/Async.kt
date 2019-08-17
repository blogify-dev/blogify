package blgoify.backend.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.jetbrains.exposed.sql.transactions.transaction

suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) { transaction { block() } }