package blogify.backend.routes

import blogify.backend.database.Uploadables
import blogify.backend.database.handling.query
import blogify.backend.util.hex
import com.andreapivetta.kolor.green

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import org.jetbrains.exposed.sql.insert

import org.slf4j.LoggerFactory

import java.io.File

import kotlin.math.absoluteValue
import kotlin.random.Random

private val logger = LoggerFactory.getLogger("blogify-static-uploader")

private val STATIC_CONTENT_FILE_SIGNATURE = byteArrayOf(0x6c, 0x75, 0x63, 0x79)

fun Route.static() {

    post("/upload") {
        val multiPartData = call.receiveMultipart()

        val uploadableId = Random.Default
            .nextLong().absoluteValue
            .toString(16)
            .toUpperCase()
        var collectionName = ""
        var fileBytes = byteArrayOf()
        var fileContentType: ContentType = ContentType.Application.Any

        multiPartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "collection") collectionName = part.value
                }
                is PartData.FileItem -> {
                    part.streamProvider().use { input -> fileBytes = input.readBytes() }
                    fileContentType = part.contentType ?: ContentType.Application.Any
                }
            }
        }

        val outFile = File("/var/static/$collectionName-$uploadableId.bin")
        outFile.writeBytes(byteArrayOf(*STATIC_CONTENT_FILE_SIGNATURE, *fileContentType.toString().toByteArray(), 0x00, *fileBytes))

        query {
            Uploadables.insert {
                it[id] = uploadableId.toLong(radix = 16)
                it[collection] = collectionName
                it[contentType] = fileContentType.toString()
            }
        }

        logger.debug("""${"\n"}
            File uploaded {
                Id :           $uploadableId
                Collection :   $collectionName
                Content-Type : $fileContentType
                File name :    ${outFile.name}
                Signature :    ${byteArrayOf(*STATIC_CONTENT_FILE_SIGNATURE, *fileContentType.toString().toByteArray(), 0x00)
                                    .joinToString(" ", transform = Byte::hex)}
            }
        """.trimIndent().green())

        call.respond(HttpStatusCode.Created)

    }

    get("/get/{collection}/{uploadableId}") {

        call.parameters["collection"]?.let { collection ->

            call.parameters["uploadableId"]?.let { id ->

                val uId = id.toUpperCase()

                val file        = File("/var/static/$collection-$uId.bin")
                val rawBytes    = file.readBytes().drop(STATIC_CONTENT_FILE_SIGNATURE.size)
                val contentType = String(rawBytes.takeWhile { it != 0x00.toByte() }.toByteArray())
                val content     = rawBytes.dropWhile { it != 0x00.toByte() }.drop(1).toByteArray()

                call.respondBytes(content, ContentType.parse(contentType))

            }

        }

    }

}
