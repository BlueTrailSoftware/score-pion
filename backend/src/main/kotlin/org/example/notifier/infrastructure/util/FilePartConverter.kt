package org.example.notifier.infrastructure.util

import org.springframework.http.codec.multipart.FilePart
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlinx.coroutines.reactive.awaitSingle

object FilePartConverter {

    /**
     * Converts a FilePart to InputStream
     */
    suspend fun toInputStream(filePart: FilePart): InputStream {
        // Collect all data buffers into bytes
        val byteArray = filePart.content()
            .map { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                bytes
            }
            .collectList()
            .awaitSingle()
            .flatMap { it.toList() }
            .toByteArray()

        return ByteArrayInputStream(byteArray)
    }

    /**
     * Gets the file size from FilePart headers
     */
    fun getFileSize(filePart: FilePart): Long {
        return filePart.headers().contentLength ?: 0
    }

    /**
     * Gets the filename from FilePart
     */
    fun getFileName(filePart: FilePart): String? {
        return filePart.filename()
    }

    /**
     * Gets the content type from FilePart
     */
    fun getContentType(filePart: FilePart): String? {
        return filePart.headers().contentType?.toString()
    }
}