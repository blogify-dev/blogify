package blgoify.backend.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * DO NOT USE THIS IN A NESTED QUERY ! EXPOSED BUG
 */
suspend fun <T> query(block: suspend () -> T): T = withContext(Dispatchers.IO) { newSuspendedTransaction { block() } }