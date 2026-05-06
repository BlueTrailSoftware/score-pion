package org.example.notifier.application.useCases.getApplicantById

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.position.OpenPosition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetApplicantByIdUseCaseTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetApplicantByIdUseCase

    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        applicantService = mock(ApplicantService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        useCase = GetApplicantByIdUseCase(applicantService, openPositionService)
    }

    @Test
    fun `execute returns applicant with position title`() = runBlocking<Unit> {
        val applicant = Applicant(
            id = "app-1", name = "Test", email = "t@e.com", phone = null,
            positionId = "pos-1", status = ApplicantStatus.PENDING,
            deleteAfter = now.plusMonths(9), createdAt = now, updatedAt = now
        )
        val position = OpenPosition(id = "pos-1", title = "Developer", description = "desc", createdBy = "admin-1", createdAt = now, updatedAt = now)

        whenever(applicantService.getApplicantById("app-1")).thenReturn(applicant)
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)

        val result = useCase.execute(GetApplicantByIdCommand("app-1"))

        assertNotNull(result)
        assertEquals("Developer", result!!.positionTitle)
    }

    @Test
    fun `execute returns null when applicant not found`() = runBlocking<Unit> {
        whenever(applicantService.getApplicantById("missing")).thenReturn(null)

        val result = useCase.execute(GetApplicantByIdCommand("missing"))

        assertNull(result)
    }

    @Test
    fun `execute returns null position title when position not found`() = runBlocking<Unit> {
        val applicant = Applicant(
            id = "app-1", name = "Test", email = "t@e.com", phone = null,
            positionId = "pos-x", status = ApplicantStatus.PENDING,
            deleteAfter = now.plusMonths(9), createdAt = now, updatedAt = now
        )

        whenever(applicantService.getApplicantById("app-1")).thenReturn(applicant)
        whenever(openPositionService.getPosition("pos-x")).thenReturn(null)

        val result = useCase.execute(GetApplicantByIdCommand("app-1"))

        assertNotNull(result)
        assertNull(result!!.positionTitle)
    }
}
