package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.port.ApplicantRepository
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.time.LocalDateTime

class ApplicantServiceImplAnonymizeTest {

    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var fileService: FileService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var logger: LoggerPort
    private lateinit var service: ApplicantServiceImpl

    @BeforeEach
    fun setUp() {
        applicantRepository = mock(ApplicantRepository::class.java)
        fileService = mock(FileService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        logger = mock(LoggerPort::class.java)
        service = ApplicantServiceImpl(
            applicantRepository, openPositionService, fileService, logger
        )
    }

    private fun buildApplicant(
        id: String = "app-1",
        email: String = "john@test.com",
        fileUrl: String? = null,
        status: ApplicantStatus = ApplicantStatus.PENDING
    ) = Applicant(
        id = id,
        name = "John Doe",
        email = email,
        phone = "+1234567890",
        positionId = "pos-1",
        status = status,
        fileUrl = fileUrl,
        linkedinUrl = "https://linkedin.com/in/john",
        deleteAfter = LocalDateTime.now().plusMonths(9)
    )

    // ===== anonymizeSingleApplicant =====

    @Test
    fun `anonymizeSingleApplicant replaces PII fields and sets status to ANONYMIZED`() = runBlocking<Unit> {
        val applicant = buildApplicant()
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.anonymizeSingleApplicant(applicant)

        val savedCaptor = argumentCaptor<Applicant>()
        verify(applicantRepository).save(savedCaptor.capture())
        val saved = savedCaptor.firstValue

        assertEquals("DELETED_USER", saved.name)
        assertEquals("deleted-app-1@anonymized.local", saved.email)
        assertNull(saved.phone)
        assertNull(saved.linkedinUrl)
        assertNull(saved.fileUrl)
        assertEquals(ApplicantStatus.ANONYMIZED, saved.status)
        assertTrue(result.success)
        assertFalse(result.fileDeleted)
    }

    @Test
    fun `anonymizeSingleApplicant with fileUrl calls hardDelete and sets fileDeleted true`() = runBlocking<Unit> {
        val applicant = buildApplicant(fileUrl = "https://s3.example.com/cv.pdf")
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.anonymizeSingleApplicant(applicant)

        verify(fileService).hardDelete("https://s3.example.com/cv.pdf")
        assertTrue(result.fileDeleted)
        assertTrue(result.success)
    }

    @Test
    fun `anonymizeSingleApplicant with null fileUrl does not call hardDelete`() = runBlocking<Unit> {
        val applicant = buildApplicant(fileUrl = null)
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.anonymizeSingleApplicant(applicant)

        verifyNoInteractions(fileService)
        assertFalse(result.fileDeleted)
        assertTrue(result.success)
    }

    @Test
    fun `anonymizeSingleApplicant when hardDelete throws still succeeds with fileDeleted false`() = runBlocking<Unit> {
        val applicant = buildApplicant(fileUrl = "https://s3.example.com/cv.pdf")
        whenever(fileService.hardDelete(any())).thenThrow(RuntimeException("S3 error"))
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.anonymizeSingleApplicant(applicant)

        assertTrue(result.success)
        assertFalse(result.fileDeleted)
        verify(applicantRepository).save(any())
    }

    // ===== anonymizeApplicantsByEmail =====

    @Test
    fun `anonymizeApplicantsByEmail with two applicants anonymizes each and returns success count`() = runBlocking<Unit> {
        val applicant1 = buildApplicant(id = "app-1")
        val applicant2 = buildApplicant(id = "app-2")
        whenever(applicantRepository.findByEmail("john@test.com")).thenReturn(listOf(applicant1, applicant2))
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] }

        val count = service.anonymizeApplicantsByEmail("john@test.com")

        assertEquals(2, count)
        verify(applicantRepository, times(2)).save(any())
    }

    @Test
    fun `anonymizeApplicantsByEmail with no applicants returns 0 and does not save`() = runBlocking<Unit> {
        whenever(applicantRepository.findByEmail("nobody@test.com")).thenReturn(emptyList())

        val count = service.anonymizeApplicantsByEmail("nobody@test.com")

        assertEquals(0, count)
        verify(applicantRepository, never()).save(any())
    }
}
