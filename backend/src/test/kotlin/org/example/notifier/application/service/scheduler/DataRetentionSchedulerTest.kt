package org.example.notifier.application.service.scheduler

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.AnonymizationResult
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.port.ApplicantRepository
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.time.LocalDateTime

class DataRetentionSchedulerTest {

    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var applicantService: ApplicantService
    private lateinit var logger: LoggerPort
    private lateinit var scheduler: DataRetentionScheduler

    @BeforeEach
    fun setUp() {
        applicantRepository = mock(ApplicantRepository::class.java)
        applicantService = mock(ApplicantService::class.java)
        logger = mock(LoggerPort::class.java)
        scheduler = DataRetentionScheduler(applicantRepository, applicantService, logger)
    }

    private fun buildApplicant(
        id: String = "app-1",
        deleteAfter: LocalDateTime = LocalDateTime.now().minusDays(1),
        status: ApplicantStatus = ApplicantStatus.PENDING
    ) = Applicant(
        id = id,
        name = "John Doe",
        email = "john@test.com",
        phone = "+1234567890",
        positionId = "pos-1",
        status = status,
        deleteAfter = deleteAfter
    )

    @Test
    fun `cleanupExpiredData anonymizes expired non-anonymized applicants`() = runBlocking<Unit> {
        val expired1 = buildApplicant("app-1", deleteAfter = LocalDateTime.now().minusDays(1))
        val expired2 = buildApplicant("app-2", deleteAfter = LocalDateTime.now().minusDays(30))
        whenever(applicantRepository.findAll()).thenReturn(listOf(expired1, expired2))
        whenever(applicantService.anonymizeSingleApplicant(any()))
            .thenReturn(AnonymizationResult("app-1", fileDeleted = false, success = true))

        scheduler.cleanupExpiredData()

        verify(applicantService, times(2)).anonymizeSingleApplicant(any())
    }

    @Test
    fun `cleanupExpiredData skips applicants already ANONYMIZED`() = runBlocking<Unit> {
        val expired = buildApplicant("app-1", deleteAfter = LocalDateTime.now().minusDays(1))
        val alreadyAnonymized = buildApplicant("app-2", deleteAfter = LocalDateTime.now().minusDays(10), status = ApplicantStatus.ANONYMIZED)
        whenever(applicantRepository.findAll()).thenReturn(listOf(expired, alreadyAnonymized))
        whenever(applicantService.anonymizeSingleApplicant(any()))
            .thenReturn(AnonymizationResult("app-1", fileDeleted = false, success = true))

        scheduler.cleanupExpiredData()

        verify(applicantService, times(1)).anonymizeSingleApplicant(expired)
        verify(applicantService, never()).anonymizeSingleApplicant(alreadyAnonymized)
    }

    @Test
    fun `cleanupExpiredData with no expired applicants does not call anonymize`() = runBlocking<Unit> {
        val future = buildApplicant("app-1", deleteAfter = LocalDateTime.now().plusMonths(6))
        whenever(applicantRepository.findAll()).thenReturn(listOf(future))

        scheduler.cleanupExpiredData()

        verifyNoInteractions(applicantService)
    }

    @Test
    fun `cleanupExpiredData when findAll throws does not propagate exception`() = runBlocking<Unit> {
        whenever(applicantRepository.findAll()).thenThrow(RuntimeException("DynamoDB error"))

        scheduler.cleanupExpiredData()

        verifyNoInteractions(applicantService)
        verify(logger).error(any<String>(), any<Throwable>())
    }
}
