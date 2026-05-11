package org.example.notifier.application.service.file

import kotlinx.coroutines.runBlocking
import org.example.notifier.domain.port.FileStoragePort
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart

class FileServiceTest {

    private lateinit var fileStoragePort: FileStoragePort
    private lateinit var logger: LoggerPort
    private lateinit var fileService: FileService

    private val bucketName = "test-bucket"
    // URL that resolves to key "candidates/app-1/file.pdf" via extractKeyFromUrl
    private val existingUrl = "https://s3.amazonaws.com/$bucketName/candidates/app-1/file.pdf"
    private val existingKey = "candidates/app-1/file.pdf"

    @BeforeEach
    fun setUp() {
        fileStoragePort = mock(FileStoragePort::class.java)
        logger = mock(LoggerPort::class.java)
        fileService = FileService(bucketName, fileStoragePort, logger)
    }

    // --- handleFileUpdate: no-op ---

    @Test
    fun `handleFileUpdate returns current URL unchanged when no action requested`() = runBlocking<Unit> {
        val result = fileService.handleFileUpdate(
            currentFileUrl = existingUrl,
            newFilePart = null,
            deleteFile = false,
            entityType = "candidates",
            entityId = "app-1"
        )

        assertEquals(existingUrl, result.fileUrl)
        assertFalse(result.isFileDeleted)
        verify(fileStoragePort, never()).softDeleteFile(any(), any())
        verify(fileStoragePort, never()).uploadFile(any(), any())
    }

    @Test
    fun `handleFileUpdate returns null unchanged when no file and no action`() = runBlocking<Unit> {
        val result = fileService.handleFileUpdate(
            currentFileUrl = null,
            newFilePart = null,
            deleteFile = false,
            entityType = "candidates",
            entityId = "app-1"
        )

        assertNull(result.fileUrl)
        assertFalse(result.isFileDeleted)
    }

    // --- handleFileUpdate: delete ---

    @Test
    fun `handleFileUpdate soft-deletes current file and returns null when deleteFile is true`() = runBlocking<Unit> {
        whenever(fileStoragePort.softDeleteFile(existingKey, "candidates"))
            .thenReturn("deleted/candidates/app-1/file.pdf")

        val result = fileService.handleFileUpdate(
            currentFileUrl = existingUrl,
            newFilePart = null,
            deleteFile = true,
            entityType = "candidates",
            entityId = "app-1"
        )

        assertNull(result.fileUrl)
        assertTrue(result.isFileDeleted)
        verify(fileStoragePort).softDeleteFile(existingKey, "candidates")
    }

    @Test
    fun `handleFileUpdate skips soft-delete and returns null when deleteFile is true but no current file`() = runBlocking<Unit> {
        val result = fileService.handleFileUpdate(
            currentFileUrl = null,
            newFilePart = null,
            deleteFile = true,
            entityType = "candidates",
            entityId = "app-1"
        )

        assertNull(result.fileUrl)
        assertFalse(result.isFileDeleted)
        verify(fileStoragePort, never()).softDeleteFile(any(), any())
    }

    // --- handleFileUpdate: upload ---

    @Test
    fun `handleFileUpdate uploads new file and returns new URL when no previous file`() = runBlocking<Unit> {
        val filePart = buildFilePart("cv.pdf", "application/pdf")
        val newUrl = "https://s3.amazonaws.com/$bucketName/candidates/app-1/new_cv.pdf"
        whenever(fileStoragePort.uploadFile(any(), any())).thenReturn(newUrl)

        val result = fileService.handleFileUpdate(
            currentFileUrl = null,
            newFilePart = filePart,
            deleteFile = false,
            entityType = "candidates",
            entityId = "app-1"
        )

        assertEquals(newUrl, result.fileUrl)
        assertFalse(result.isFileDeleted)
        verify(fileStoragePort).uploadFile(any(), any())
        verify(fileStoragePort, never()).softDeleteFile(any(), any())
    }

    @Test
    fun `handleFileUpdate soft-deletes old file before uploading new one`() = runBlocking<Unit> {
        val filePart = buildFilePart("new_cv.pdf", "application/pdf")
        val newUrl = "https://s3.amazonaws.com/$bucketName/candidates/app-1/new_cv.pdf"
        whenever(fileStoragePort.softDeleteFile(existingKey, "candidates"))
            .thenReturn("deleted/candidates/app-1/file.pdf")
        whenever(fileStoragePort.uploadFile(any(), any())).thenReturn(newUrl)

        val result = fileService.handleFileUpdate(
            currentFileUrl = existingUrl,
            newFilePart = filePart,
            deleteFile = false,
            entityType = "candidates",
            entityId = "app-1"
        )

        assertEquals(newUrl, result.fileUrl)
        assertFalse(result.isFileDeleted)
        verify(fileStoragePort).softDeleteFile(existingKey, "candidates")
        verify(fileStoragePort).uploadFile(any(), any())
    }

    // --- helpers ---

    private fun buildFilePart(filename: String, contentType: String): FilePart {
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType(contentType)
        return mock(FilePart::class.java).also {
            whenever(it.filename()).thenReturn(filename)
            whenever(it.headers()).thenReturn(headers)
        }
    }
}