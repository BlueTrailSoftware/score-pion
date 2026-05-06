package org.example.notifier.application.service.file

import org.springframework.beans.factory.annotation.Value
import org.example.notifier.domain.port.FileStoragePort
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class FileService(
    @Value("\${aws.s3.bucket-name}") private val bucketName: String,
    private val fileStoragePort: FileStoragePort,
    private val logger: LoggerPort
) {

    data class FileUpdateResult(
        val fileUrl: String?,
        val isFileDeleted: Boolean = false
    )

    suspend fun handleFileUpdate(
        currentFileUrl: String?,
        newFilePart: FilePart?,
        deleteFile: Boolean,
        entityType: String,
        entityId: String
    ): FileUpdateResult {
        return when {
            deleteFile && currentFileUrl != null -> {
                softDelete(currentFileUrl, entityType)
                FileUpdateResult(null, true)
            }

            newFilePart != null -> {
                val newFileUrl = uploadOrReplace(currentFileUrl, newFilePart, entityType, entityId)
                FileUpdateResult(newFileUrl, false)
            }

            else -> FileUpdateResult(currentFileUrl, false)
        }
    }

    private suspend fun uploadOrReplace(
        currentFileUrl: String?,
        newFilePart: FilePart,
        entityType: String,
        entityId: String
    ): String {
        currentFileUrl?.let { softDelete(it, entityType) }

        return upload(newFilePart, entityType, entityId)
    }

    suspend fun upload(
        filePart: FilePart,
        entityType: String,
        entityId: String
    ): String {
        val fileName = filePart.filename()
            ?: throw IllegalArgumentException("File name is required")

        val sanitizedName = sanitizeFileName(fileName)
        val key = buildS3Key(entityType, entityId, sanitizedName)

        return try {
            val url = fileStoragePort.uploadFile(key, filePart)
            logger.info("File uploaded for $entityType $entityId: $fileName -> $url")
            url
        } catch (e: Exception) {
            logger.error("Failed to upload file: $fileName", e)
            throw IllegalArgumentException("Failed to upload file: ${e.message}")
        }
    }

    suspend fun hardDelete(fileUrl: String) {
        val key = extractKeyFromUrl(fileUrl)
        if (key.isNotBlank()) {
            fileStoragePort.deleteFile(key)
            logger.info("File permanently deleted: $key")
        }
    }

    private suspend fun softDelete(fileUrl: String, entityType: String) {
        val key = extractKeyFromUrl(fileUrl)
        if (key.isNotBlank()) {
            try {
                fileStoragePort.softDeleteFile(key, entityType)
                logger.info("File moved to deleted folder: $key")
            } catch (e: Exception) {
                logger.warn("Failed to soft delete file: $key", e)
            }
        }
    }

    private fun buildS3Key(entityType: String, entityId: String, fileName: String): String {
        val timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val uniqueId = UUID.randomUUID().toString().take(8)
        return "$entityType/$entityId/${timestamp}_${uniqueId}_$fileName"
    }

    private fun extractKeyFromUrl(fileUrl: String): String {
        val cleanUrl = fileUrl
            .replace("http://", "")
            .replace("https://", "")

        val bucketIndex = cleanUrl.indexOf("$bucketName/")
        if (bucketIndex != -1) {
            return cleanUrl.substring(bucketIndex + bucketName.length + 1)
        }

        val firstSlashIndex = cleanUrl.indexOf('/')
        if (firstSlashIndex != -1) {
            val afterFirstSlash = cleanUrl.substring(firstSlashIndex + 1)
            val secondSlashIndex = afterFirstSlash.indexOf('/')
            if (secondSlashIndex != -1) {
                return afterFirstSlash.substring(secondSlashIndex + 1)
            }
            return afterFirstSlash
        }

        return cleanUrl
    }

    private fun sanitizeFileName(fileName: String): String {
        val baseName = fileName.substringBeforeLast('.', "")
        val extension = fileName.substringAfterLast('.', "")

        val cleanBase = baseName
            .replace(" ", "_")
            .replace("[^a-zA-Z0-9_-]".toRegex(), "")
            .take(50)

        return if (extension.isNotBlank()) "$cleanBase.$extension" else cleanBase
    }
}