package org.example.notifier.application.useCases.updateApplicant

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.position.OpenPosition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class UpdateApplicantUseCaseTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var fileService: FileService
    private lateinit var useCase: UpdateApplicantUseCase

    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        applicantService = mock(ApplicantService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        fileService = mock(FileService::class.java)
        useCase = UpdateApplicantUseCase(applicantService, openPositionService, fileService)
    }

    @Test
    fun `execute updates applicant and enriches with position title`() = runBlocking<Unit> {
        val command = UpdateApplicantCommand("app-1", "New Name", "new@e.com", "+1234567890", null, false)
        val existingApplicant = Applicant(
            id = "app-1", name = "Old Name", email = "old@e.com", phone = null,
            positionId = "pos-1", status = ApplicantStatus.PENDING,
            deleteAfter = now.plusMonths(9), createdAt = now, updatedAt = now
        )
        val updatedApplicant = existingApplicant.copy(name = "New Name", email = "new@e.com", phone = "+1234567890")
        val position = OpenPosition(id = "pos-1", title = "Designer", description = "desc", createdBy = "admin-1", createdAt = now, updatedAt = now)

        whenever(applicantService.getApplicantById("app-1")).thenReturn(existingApplicant)
        whenever(fileService.handleFileUpdate(null, null, false, "candidates", "app-1"))
            .thenReturn(FileService.FileUpdateResult(null, false))
        whenever(applicantService.updateApplicant("app-1", "New Name", "new@e.com", "+1234567890", null, false)).thenReturn(updatedApplicant)
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)

        val result = useCase.execute(command)

        assertEquals("New Name", result.name)
        assertEquals("Designer", result.positionTitle)
    }

    @Test
    fun `execute returns null title when position not found`() = runBlocking<Unit> {
        val command = UpdateApplicantCommand("app-1", null, null, null, null, true)
        val existingApplicant = Applicant(
            id = "app-1", name = "Test", email = "t@e.com", phone = null,
            positionId = "pos-x", status = ApplicantStatus.PENDING,
            deleteAfter = now.plusMonths(9), createdAt = now, updatedAt = now
        )

        whenever(applicantService.getApplicantById("app-1")).thenReturn(existingApplicant)
        whenever(fileService.handleFileUpdate(null, null, true, "candidates", "app-1"))
            .thenReturn(FileService.FileUpdateResult(null, true))
        whenever(applicantService.updateApplicant("app-1", null, null, null, null, true)).thenReturn(existingApplicant)
        whenever(openPositionService.getPosition("pos-x")).thenReturn(null)

        val result = useCase.execute(command)

        assertNull(result.positionTitle)
    }
}
