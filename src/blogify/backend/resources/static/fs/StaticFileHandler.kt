package blogify.backend.resources.static.fs

import blogify.backend.resources.static.models.StaticData
import blogify.backend.resources.static.models.StaticResourceHandle

import io.ktor.http.ContentType

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlin.random.Random

import java.io.File

/**
 * Utility object for storing [StaticData] objects in the filesystem
 *
 * @author Benjozork, hamza1311
 */
object StaticFileHandler {

    private const val BASE_STATIC_FILE_PATH = "/var/static"
    private const val STATIC_FILE_EXTENSION = "blogifystatic"
    private val STATIC_FILE_SIGNATURE = byteArrayOf(0x6c, 0x75, 0x63, 0x79)

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

        val file        = File("$BASE_STATIC_FILE_PATH/$fileId.$STATIC_FILE_EXTENSION")
        val rawBytes    = file.readBytes().drop(STATIC_FILE_SIGNATURE.size)
        val contentType = String(rawBytes.takeWhile { it != 0x00.toByte() }.toByteArray())
        val content     = rawBytes.dropWhile { it != 0x00.toByte() }.drop(1).toByteArray()

        return@withContext StaticData(ContentType.parse(contentType), content)
    }

    /**
     * Writes a file containing an uploaded [StaticData] onto the filesystem
     *
     * @param baseHandle the original handle that should be updated
     * @param staticData the actual byte data of the uploaded static resource
     *
     * @return a [StaticResourceHandle.Ok] describing the stored [static resource][StaticData]
     *
     * @author Benjozork
     */
    suspend fun writeStaticResource (
        baseHandle: StaticResourceHandle,
        staticData: StaticData
    ): StaticResourceHandle.Ok = withContext(IO) {

        // Generate random ID
        val fileId = Random.Default.nextLong()

        // Create file from base name, fileId and extension
        val targetFile = File("$BASE_STATIC_FILE_PATH/$fileId.$STATIC_FILE_EXTENSION")

        // Write contents
        targetFile.writeBytes (
             STATIC_FILE_SIGNATURE
                + staticData.contentType.toString().toByteArray()
                + 0x00
                + staticData.bytes
        )

        // Return created handle
        return@withContext StaticResourceHandle.Ok(baseHandle, fileId)
    }

}