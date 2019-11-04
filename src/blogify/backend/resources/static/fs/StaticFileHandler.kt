package blogify.backend.resources.static.fs

import blogify.backend.resources.static.models.StaticData
import blogify.backend.resources.static.models.StaticResourceHandle
import com.andreapivetta.kolor.red

import io.ktor.http.ContentType

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlin.random.Random

import org.slf4j.LoggerFactory

import java.io.File
import java.lang.Exception

/**
 * Utility object for storing [StaticData] objects in the filesystem
 *
 * @author Benjozork, hamza1311
 */
object StaticFileHandler {

    private const val BASE_STATIC_FILE_PATH = "/var/static"
    private const val STATIC_FILE_EXTENSION = "blogifystatic"
    private val STATIC_FILE_SIGNATURE = byteArrayOf(0x6c, 0x75, 0x63, 0x79)

    private val logger = LoggerFactory.getLogger("blogify-static-fs")

    private fun getTargetFileFromId(fileId: String) = File("$BASE_STATIC_FILE_PATH/$fileId.$STATIC_FILE_EXTENSION")

    /**
     * Reads a file from filesystem and returns its [StaticData]
     *
     * @param fileId id of the file to read.
     *
     * @return [StaticData] of the requested file
     *
     * @author hamza1311
     */
    suspend fun readStaticResource(fileId: Long): StaticData = withContext(IO) {

        val targetFile: File = getTargetFileFromId(fileId.toString())
        val rawBytes:  ByteArray
        val contentType: String
        val content: ByteArray

        try {
            rawBytes    = targetFile.readBytes().drop(STATIC_FILE_SIGNATURE.size).toByteArray()
            contentType = String(rawBytes.takeWhile { it != 0x00.toByte() }.toByteArray())
            content     = rawBytes.dropWhile { it != 0x00.toByte() }.drop(1).toByteArray()
        } catch (e: Exception) {
            logger.error("couldn't read static file ${targetFile.name}: ${e::class.simpleName} - ${e.message}")
            return@withContext StaticData(ContentType.Any, byteArrayOf())
        }

        return@withContext StaticData(ContentType.parse(contentType), content)
    }

    /**
     * Writes a file containing an uploaded [StaticData] onto the filesystem
     *
     * @param staticData the actual byte data of the uploaded static resource
     *
     * @return a [StaticResourceHandle.Ok] describing the stored [static resource][StaticData]
     *
     * @author Benjozork
     */
    suspend fun writeStaticResource (
        staticData: StaticData
    ): StaticResourceHandle.Ok = withContext(IO) {

        // Generate random ID
        val fileId = Random.Default.nextLong(0, Long.MAX_VALUE).toString()

        // Create file from base name, fileId and extension
        val targetFile = getTargetFileFromId(fileId)

        // Write contents
        try {
            targetFile.writeBytes (
                 STATIC_FILE_SIGNATURE
                    + staticData.contentType.toString().toByteArray()
                    + 0x00
                    + staticData.bytes
            )
        } catch (e: Exception) {
            logger.error("couldn't write static file ${targetFile.name}: ${e::class.simpleName} - ${e.message}")
        }

        // Return created handle
        return@withContext StaticResourceHandle.Ok(staticData.contentType, fileId)
    }

    /**
     * Deletes a file containing [StaticData] on the filesystem
     *
     * @param handle a [StaticResourceHandle.Ok] pointing to the stored [static resource][StaticData]
     *
     * @return whether or not the file was successfully deleted
     *
     * @author Benjozork
     */
    suspend fun deleteStaticResource (
        handle: StaticResourceHandle.Ok
    ): Boolean = withContext(IO) {
        val targetFile = getTargetFileFromId(handle.fileId)

        try {
            if (targetFile.delete()) {
                true
            } else {
                logger.error("couldn't delete static file ${targetFile.name}: unknown reason (probably doesn't exist)".red())
                false
            }
        } catch (e: Exception) {
            logger.error("couldn't delete static file ${targetFile.name}: ${e::class.simpleName} - ${e.message}".red())
            false
        }
    }

}
