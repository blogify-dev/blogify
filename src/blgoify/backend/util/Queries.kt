package blgoify.backend.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * This function executes a query asynchronously and returns its result.
 *
 * **DO NOT USE THIS IN A NESTED QUERY ! (EXPOSED BUG)**
 *
 * @param block the query block to execute
 *
 * @see [Dispatchers.IO]
 */
suspend fun <T> query(block: suspend () -> T): T = withContext(Dispatchers.IO) { newSuspendedTransaction { block() } }

/**
 * This function executes a query asynchronously and returns true if no exception occurred.
 *
 * It returns false if an exception occurred and prints the stack trace.
 *
 * **DO NOT USE THIS IN A NESTED QUERY ! (EXPOSED BUG)**
 *
 * @param block the query block to execute
 *
 * @see [Dispatchers.IO]
 */
@Suppress("unused")
suspend fun <T> booleanReturnQuery(block: suspend () -> T): Boolean {
    return try { query(block); true }
    catch(e: Exception) { e.printStackTrace(); false }
}