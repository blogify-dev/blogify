package blogify.backend.routes

import blogify.backend.util.hex

import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.routing.Route
import io.ktor.routing.post

import org.slf4j.LoggerFactory

import java.io.File

private val logger = LoggerFactory.getLogger("blogify-static-uploader")

fun Route.static() {

    post("/upload") {
        val multiPartData = call.receiveMultipart()

        var collectionName = ""
        var fileBytes = byteArrayOf()

        multiPartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "collection") collectionName = part.value
                }
                is PartData.FileItem -> {
                    part.streamProvider().use { input -> fileBytes = input.readBytes() }
                }
            }
        }

        println("Collection: $collectionName")
        println("Bytes: ${fileBytes.joinToString(separator = " ", transform = Byte::hex)}")

        val outFile = File("/var/static/$collectionName-${System.currentTimeMillis() / 1000L}.bin")
        outFile.writeBytes(fileBytes)
    }

}
