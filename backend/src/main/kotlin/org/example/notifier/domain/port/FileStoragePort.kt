package org.example.notifier.domain.port

import org.springframework.http.codec.multipart.FilePart

interface FileStoragePort {
    suspend fun uploadFile(key: String, filePart: FilePart): String
    suspend fun deleteFile(key: String)
    suspend fun softDeleteFile(key: String, entityType: String): String
}