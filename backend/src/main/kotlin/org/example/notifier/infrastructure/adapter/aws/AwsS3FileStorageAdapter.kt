package org.example.notifier.infrastructure.adapter.aws

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import org.example.notifier.domain.port.FileStoragePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import kotlinx.coroutines.reactive.awaitSingle
import java.util.UUID

@Component
class AwsS3FileStorageAdapter(
    private val s3Client: S3Client
) : FileStoragePort {

    @Value("\${aws.s3.bucket-name}")
    private lateinit var bucketName: String

    @Value("\${aws.s3.files.endpoint}")
    private lateinit var filesEndpoint: String

    override suspend fun uploadFile(key: String, filePart: FilePart): String {
        val bytes = filePart.content()
            .map { dataBuffer ->
                val byteArray = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(byteArray)
                byteArray
            }
            .collectList()
            .awaitSingle()
            .flatMap { it.toList() }
            .toByteArray()

        val request = PutObjectRequest {
            bucket = bucketName
            this.key = key
            body = ByteStream.fromBytes(bytes)
            contentType = filePart.headers().contentType?.toString()
        }

        s3Client.putObject(request)

        return "$filesEndpoint/$key"
    }

    override suspend fun deleteFile(key: String) {
        val request = DeleteObjectRequest {
            bucket = bucketName
            this.key = key
        }
        s3Client.deleteObject(request)
    }

    override suspend fun softDeleteFile(key: String, entityType: String): String {
        val timestamp = System.currentTimeMillis()
        val fileName = key.substringAfterLast('/')
        val deletedKey = "deleted/$entityType/$timestamp-$fileName"

        // Copy to deleted folder
        val copyRequest = aws.sdk.kotlin.services.s3.model.CopyObjectRequest {
            bucket = bucketName
            copySource = "$bucketName/$key"
            this.key = deletedKey
        }
        s3Client.copyObject(copyRequest)

        // Delete original
        deleteFile(key)

        return "$filesEndpoint/$deletedKey"
    }

    fun generateKey(prefix: String, fileName: String): String {
        val cleanName = fileName
            .replace(" ", "-")
            .replace("[^a-zA-Z0-9.\\-]".toRegex(), "")
            .lowercase()
        val shortId = UUID.randomUUID().toString().take(8)
        return "$prefix/$shortId-$cleanName"
    }
}