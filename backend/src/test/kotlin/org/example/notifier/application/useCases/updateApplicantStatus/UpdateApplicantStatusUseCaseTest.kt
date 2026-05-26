package org.example.notifier.application.useCases.updateApplicantStatus

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.integration.AssessmentInfo
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.event.ApplicantStatusChangedEvent
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class UpdateApplicantStatusUseCaseTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var assessmentPlatformService: AssessmentPlatformService
    private lateinit var invitationService: InvitationService
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var logger: LoggerPort
    private lateinit var useCase: UpdateApplicantStatusUseCase

    private val now = LocalDateTime.now()
    private val reviewer = User(id = "admin-1", email = "admin@test.com", name = "Admin", role = "ADMIN")
    private val positionId = "pos-1"

    private val availableAssessments = listOf(
        AssessmentInfo(id = "a-1", title = "Java Test", publicUrl = "https://coderbyte.com/a1")
    )

    @BeforeEach
    fun setUp() {
        applicantService = mock(ApplicantService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        assessmentPlatformService = mock(AssessmentPlatformService::class.java)
        invitationService = mock(InvitationService::class.java)
        eventPublisher = mock(ApplicationEventPublisher::class.java)
        logger = mock(LoggerPort::class.java)
        useCase = UpdateApplicantStatusUseCase(
            applicantService,
            openPositionService,
            assessmentPlatformService,
            invitationService,
            eventPublisher,
            logger
        )
    }

    // --- status update + return value ---

    @Test
    fun `execute updates status and enriches with position title`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.PENDING)
        val position = buildPosition("Developer")
        whenever(applicantService.updateApplicantStatus("app-1", "PENDING", reviewer, null))
            .thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(UpdateApplicantStatusCommand("app-1", "PENDING", reviewer, null))

        assertEquals("PENDING", result.status)
        assertEquals("Developer", result.positionTitle)
    }

    @Test
    fun `execute returns null title when position not found`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.REJECTED)
        whenever(applicantService.updateApplicantStatus("app-1", "REJECTED", reviewer, "Not a fit"))
            .thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)

        val result = useCase.execute(UpdateApplicantStatusCommand("app-1", "REJECTED", reviewer, "Not a fit"))

        assertEquals("REJECTED", result.status)
        assertNull(result.positionTitle)
    }

    // --- INVITED events ---

    @Test
    fun `execute publishes approval event when status is INVITED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.INVITED)
        val position = buildPosition("Senior Dev")
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }

        useCase.execute(buildCommand("INVITED"))

        verify(eventPublisher).publishEvent(argThat<Any> { event ->
            event is ApplicantStatusChangedEvent
                && event.applicantEmail == "test@example.com"
                && event.applicantName == "Test"
                && event.positionTitle == "Senior Dev"
                && event.newStatus == ApplicantStatus.INVITED
                && event.reviewedBy == "admin@test.com"
        })
    }

    @Test
    fun `execute does not publish event when position not found for INVITED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.INVITED)
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }

        useCase.execute(buildCommand("INVITED"))

        verify(eventPublisher, never()).publishEvent(argThat<Any> { event -> event is ApplicantStatusChangedEvent })
    }

    @Test
    fun `execute does not throw when event publishing fails for INVITED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.INVITED)
        val position = buildPosition("Senior Dev")
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }
        whenever(eventPublisher.publishEvent(any())).thenThrow(RuntimeException("event bus down"))

        useCase.execute(buildCommand("INVITED"))
    }

    // --- REJECTED events ---

    @Test
    fun `execute publishes rejection event when status is REJECTED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.REJECTED)
        val position = buildPosition("Senior Dev")
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        useCase.execute(UpdateApplicantStatusCommand("app-1", "REJECTED", reviewer, "Not a fit"))

        verify(eventPublisher).publishEvent(argThat<Any> { event ->
            event is ApplicantStatusChangedEvent
                && event.applicantEmail == "test@example.com"
                && event.newStatus == ApplicantStatus.REJECTED
                && event.statusNote == "Not a fit"
                && event.reviewedBy == "admin@test.com"
        })
    }

    @Test
    fun `execute does not publish event when status is PENDING`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.PENDING)
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Dev"))

        useCase.execute(buildCommand("PENDING"))

        verify(eventPublisher, never()).publishEvent(argThat<Any> { event -> event is ApplicantStatusChangedEvent })
    }

    // --- assessment invitations for INVITED ---

    @Test
    fun `execute sends assessment invitations when status is INVITED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.INVITED)
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Dev"))
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }

        useCase.execute(buildCommand("INVITED"))

        verify(assessmentPlatformService).sendCandidateInvitation("test@example.com", "https://coderbyte.com/a1")
        verify(invitationService).createInvitation(argThat { inv -> inv.assessmentId == "a-1" && inv.openPositionId == positionId })
    }

    @Test
    fun `execute throws when position has no assessments for INVITED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.INVITED)
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Dev"))
        whenever(openPositionService.getPositionAssessments(positionId)).thenReturn(emptyList())

        assertThrows<IllegalArgumentException> { useCase.execute(buildCommand("INVITED")) }

        verify(assessmentPlatformService, never()).sendCandidateInvitation(any(), any())
    }

    @Test
    fun `execute skips assessment when not found in available list`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.INVITED)
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Dev"))
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("unknown-id")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)

        useCase.execute(buildCommand("INVITED"))

        verify(assessmentPlatformService, never()).sendCandidateInvitation(any(), any())
        verify(invitationService, never()).createInvitation(any())
    }

    @Test
    fun `execute does not send assessment invitations when status is REJECTED`() = runBlocking<Unit> {
        val applicant = buildApplicant(ApplicantStatus.REJECTED)
        whenever(applicantService.updateApplicantStatus(any(), any(), any(), anyOrNull())).thenReturn(applicant)
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Dev"))

        useCase.execute(buildCommand("REJECTED"))

        verify(assessmentPlatformService, never()).sendCandidateInvitation(any(), any())
        verify(invitationService, never()).createInvitation(any())
    }

    // --- helpers ---

    private fun buildCommand(status: String) =
        UpdateApplicantStatusCommand(id = "app-1", newStatus = status, reviewedBy = reviewer, statusNote = null)

    private fun buildApplicant(status: ApplicantStatus) = Applicant(
        id = "app-1",
        name = "Test",
        email = "test@example.com",
        phone = null,
        positionId = positionId,
        status = status,
        deleteAfter = now.plusMonths(9),
        createdAt = now,
        updatedAt = now
    )

    private fun buildPosition(title: String) = OpenPosition(
        id = positionId,
        title = title,
        description = "desc",
        createdBy = "admin-1"
    )

    private fun buildAssessment(assessmentId: String) = OpenPositionAssessment(
        openPositionId = positionId,
        assessmentId = assessmentId,
        assessmentName = "Test Assessment"
    )
}
