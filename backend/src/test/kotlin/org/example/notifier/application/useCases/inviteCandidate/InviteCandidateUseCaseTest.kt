package org.example.notifier.application.useCases.inviteCandidate

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.integration.AssessmentInfo
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.event.CandidateInvitedEvent
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class InviteCandidateUseCaseTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var assessmentPlatformService: AssessmentPlatformService
    private lateinit var invitationService: InvitationService
    private lateinit var userService: UserService
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var logger: LoggerPort
    private lateinit var useCase: InviteCandidateUseCase

    private val positionId = "pos-1"
    private val recruiterId = "rec-1"

    private val availableAssessments = listOf(
        AssessmentInfo(id = "a-1", title = "Java Test", publicUrl = "https://coderbyte.com/a1"),
        AssessmentInfo(id = "a-2", title = "SQL Test", publicUrl = "https://coderbyte.com/a2")
    )

    @BeforeEach
    fun setup() {
        applicantService = mock(ApplicantService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        assessmentPlatformService = mock(AssessmentPlatformService::class.java)
        invitationService = mock(InvitationService::class.java)
        userService = mock(UserService::class.java)
        eventPublisher = mock(ApplicationEventPublisher::class.java)
        logger = mock(LoggerPort::class.java)
        useCase = InviteCandidateUseCase(
            applicantService,
            openPositionService,
            assessmentPlatformService,
            invitationService,
            userService,
            eventPublisher,
            logger
        )
    }

    // --- duplicate check ---

    @Test
    fun `execute throws when candidate already self-applied (PENDING)`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId("candidate@example.com", positionId))
            .thenReturn(buildApplicant(ApplicantStatus.PENDING))

        val ex = assertThrows<IllegalArgumentException> { useCase.execute(buildCommand()) }
        assertEquals("This candidate has already applied to this position on their own", ex.message)
    }

    @Test
    fun `execute throws when candidate already self-applied (INVITED)`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId("candidate@example.com", positionId))
            .thenReturn(buildApplicant(ApplicantStatus.INVITED))

        val ex = assertThrows<IllegalArgumentException> { useCase.execute(buildCommand()) }
        assertEquals("This candidate has already applied to this position on their own", ex.message)
    }

    @Test
    fun `execute throws when candidate already self-applied (REJECTED)`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId("candidate@example.com", positionId))
            .thenReturn(buildApplicant(ApplicantStatus.REJECTED))

        val ex = assertThrows<IllegalArgumentException> { useCase.execute(buildCommand()) }
        assertEquals("This candidate has already applied to this position on their own", ex.message)
    }

    // --- email normalization ---

    @Test
    fun `execute uses normalized email for duplicate check`() = runBlocking<Unit> {
        val command = InviteCandidateCommand("  John@Example.COM  ", "Jane Doe", positionId, recruiterId)
        whenever(applicantService.findByEmailAndPositionId("john@example.com", positionId))
            .thenReturn(buildApplicant(ApplicantStatus.PENDING))

        val ex = assertThrows<IllegalArgumentException> { useCase.execute(command) }
        assertEquals("This candidate has already applied to this position on their own", ex.message)
    }

    // --- no assessments ---

    @Test
    fun `execute throws when position has no assessments`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId)).thenReturn(emptyList())

        val ex = assertThrows<IllegalArgumentException> { useCase.execute(buildCommand()) }
        assertEquals("Position $positionId has no assessments assigned", ex.message)

        verifyNoInteractions(assessmentPlatformService)
    }

    // --- assessment invitation ---

    @Test
    fun `execute invites candidate to each assessment`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test"), buildAssessment("a-2", "SQL Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }

        useCase.execute(buildCommand())

        verify(assessmentPlatformService).sendCandidateInvitation("candidate@example.com", "https://coderbyte.com/a1")
        verify(assessmentPlatformService).sendCandidateInvitation("candidate@example.com", "https://coderbyte.com/a2")
        verify(invitationService).createInvitation(argThat { inv -> inv.assessmentId == "a-1" && inv.openPositionId == positionId })
        verify(invitationService).createInvitation(argThat { inv -> inv.assessmentId == "a-2" && inv.openPositionId == positionId })
    }

    @Test
    fun `execute normalizes email and trims name before inviting`() = runBlocking<Unit> {
        val command = InviteCandidateCommand("  John@Example.COM  ", "  Jane Doe  ", positionId, recruiterId)
        whenever(applicantService.findByEmailAndPositionId("john@example.com", positionId)).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }

        useCase.execute(command)

        verify(assessmentPlatformService).sendCandidateInvitation("john@example.com", "https://coderbyte.com/a1")
        verify(invitationService).createInvitation(argThat { invitation ->
            invitation.candidateEmail == "john@example.com" && invitation.candidateName == "Jane Doe"
        })
    }

    // --- events ---

    @Test
    fun `execute publishes candidate invited event on success`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Senior Developer"))
        whenever(userService.findById(recruiterId)).thenReturn(buildRecruiter())

        useCase.execute(buildCommand())

        verify(eventPublisher).publishEvent(argThat<CandidateInvitedEvent> { event ->
            event is CandidateInvitedEvent
                && event.candidateEmail == "candidate@example.com"
                && event.candidateName == "John Doe"
                && event.positionTitle == "Senior Developer"
                && event.recruiterEmail == "recruiter@example.com"
                && event.recruiterName == "Recruiter Name"
                && event.assessmentsCount == 1
        })
    }

    @Test
    fun `execute does not publish event when all assessment invitations fail`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(assessmentPlatformService.sendCandidateInvitation(any(), any()))
            .thenThrow(RuntimeException("Coderbyte error"))

        useCase.execute(buildCommand())

        verify(eventPublisher, never()).publishEvent(argThat<Any> { event -> event is CandidateInvitedEvent })
    }

    @Test
    fun `execute does not publish event when assessment not found in available list`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("unknown-id", "Unknown")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)

        useCase.execute(buildCommand())

        verify(eventPublisher, never()).publishEvent(argThat<Any> { event -> event is CandidateInvitedEvent })
        verify(invitationService, never()).createInvitation(any())
    }

    @Test
    fun `execute publishes event when at least one assessment succeeds`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test"), buildAssessment("a-2", "SQL Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(assessmentPlatformService.sendCandidateInvitation("candidate@example.com", "https://coderbyte.com/a1"))
            .thenThrow(RuntimeException("Coderbyte error"))
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }
        whenever(openPositionService.getPosition(positionId)).thenReturn(buildPosition("Senior Developer"))
        whenever(userService.findById(recruiterId)).thenReturn(buildRecruiter())

        useCase.execute(buildCommand())

        verify(eventPublisher).publishEvent(argThat<Any> { event -> event is CandidateInvitedEvent })
    }

    @Test
    fun `execute does not publish event when createInvitation fails for all assessments`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenThrow(RuntimeException("DB error"))

        useCase.execute(buildCommand())

        verify(eventPublisher, never()).publishEvent(argThat<Any> { event -> event is CandidateInvitedEvent })
    }

    @Test
    fun `execute throws when getAvailableAssessments fails`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test")))
        whenever(assessmentPlatformService.getAvailableAssessments())
            .thenThrow(RuntimeException("Coderbyte unreachable"))

        assertThrows<RuntimeException> { useCase.execute(buildCommand()) }
    }

    @Test
    fun `execute does not publish event when position not found`() = runBlocking<Unit> {
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(openPositionService.getPositionAssessments(positionId))
            .thenReturn(listOf(buildAssessment("a-1", "Java Test")))
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(invitationService.createInvitation(any())).thenAnswer { it.arguments[0] }
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)
        whenever(userService.findById(recruiterId)).thenReturn(buildRecruiter())

        useCase.execute(buildCommand())

        verify(eventPublisher, never()).publishEvent(argThat<Any> { event -> event is CandidateInvitedEvent })
    }

    // --- helpers ---

    private fun buildCommand() = InviteCandidateCommand(
        candidateEmail = "candidate@example.com",
        candidateName = "John Doe",
        positionId = positionId,
        recruiterId = recruiterId
    )

    private fun buildApplicant(status: ApplicantStatus) = Applicant(
        id = "app-1",
        name = "John Doe",
        email = "candidate@example.com",
        phone = null,
        positionId = positionId,
        status = status,
        deleteAfter = LocalDateTime.now().plusMonths(9)
    )

    private fun buildAssessment(assessmentId: String, assessmentName: String) = OpenPositionAssessment(
        openPositionId = positionId,
        assessmentId = assessmentId,
        assessmentName = assessmentName
    )

    private fun buildPosition(title: String) = OpenPosition(
        id = positionId,
        title = title,
        description = "desc",
        createdBy = recruiterId,
        isActive = true
    )

    private fun buildRecruiter() = User(
        id = recruiterId,
        email = "recruiter@example.com",
        name = "Recruiter Name",
        role = "RECRUITER"
    )
}
