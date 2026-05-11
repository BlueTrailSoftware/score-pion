package org.example.notifier.infrastructure.util

import org.springframework.http.codec.multipart.FilePart

object FileValidator {

    private const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10 MB

    private val ALLOWED_EXTENSIONS = setOf(
        "pdf", "doc", "docx", "txt", "rtf",
        "jpg", "jpeg", "png"
    )

    private val ALLOWED_MIME_TYPES = setOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain",
        "application/rtf",
        "text/rtf",
        "image/jpeg",
        "image/png"
    )

    /**
     * Validates a FilePart for WebFlux
     */
    fun validateFile(filePart: FilePart?) {
        if (filePart == null) {
            return // No file to validate
        }

        val fileName = filePart.filename()
            ?: throw IllegalArgumentException("File name is required")

        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isBlank()) {
            throw IllegalArgumentException("File must have an extension")
        }

        if (extension !in ALLOWED_EXTENSIONS) {
            throw IllegalArgumentException(
                "File type '.$extension' is not allowed. Allowed types: ${ALLOWED_EXTENSIONS.joinToString(", ") { ".$it" }}"
            )
        }

        val mimeType = filePart.headers().contentType?.toString()
        if (mimeType.isNullOrBlank()) {
            throw IllegalArgumentException("File MIME type could not be determined")
        }

        if (mimeType !in ALLOWED_MIME_TYPES) {
            throw IllegalArgumentException(
                "File MIME type '$mimeType' is not allowed. Please upload a valid document file."
            )
        }

        val contentLength = filePart.headers().contentLength
        if (contentLength > MAX_FILE_SIZE_BYTES) {
            throw IllegalArgumentException(
                "File size exceeds maximum allowed size of ${MAX_FILE_SIZE_BYTES / (1024 * 1024)} MB"
            )
        }

        val isDocumentExtension = extension in setOf("pdf", "doc", "docx", "txt", "rtf")
        val isImageExtension = extension in setOf("jpg", "jpeg", "png")

        val isDocumentMime = mimeType.startsWith("application/") || mimeType.startsWith("text/")
        val isImageMime = mimeType.startsWith("image/")

        if (isDocumentExtension && !isDocumentMime) {
            throw IllegalArgumentException(
                "File extension and content type mismatch. Expected a document but received: $mimeType"
            )
        }

        if (isImageExtension && !isImageMime) {
            throw IllegalArgumentException(
                "File extension and content type mismatch. Expected an image but received: $mimeType"
            )
        }
    }
}