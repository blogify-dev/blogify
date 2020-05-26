package blogify.backend.database.handling

import blogify.backend.database.DatabaseConnection
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.util.getOr

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

import com.github.kittinunf.result.coroutines.SuspendableResult
import com.github.kittinunf.result.coroutines.mapError

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.lang.Exception

/**
 * This function executes a query asynchronously and returns its result.
 *
 * @param block the query block to execute
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <T : Any> query(block: suspend () -> T): SuspendableResult<T, Exception> {
    return SuspendableResult.of<T, Exception> { // The transaction can throw any Exception; specify that
        withContext(Dispatchers.IO) { newSuspendedTransaction { block() } } // Run the transaction
    }
}

/**
 * This function executes a query asynchronously and returns its result with the resulting [SuspendableResult] forcibly unwrapped.
 * Should be used for queries that should not fail in a normal situation, even if their proper use can include failures such as not found errors.
 *
 * @param block the query block to execute
 *
 * @author Benjozork
 */
@BlogifyDsl
suspend fun <T : Any> unwrappedQuery(block: suspend () -> T): T =
    query(block).getOr { error("unwrapped query resulted in a failure") }
