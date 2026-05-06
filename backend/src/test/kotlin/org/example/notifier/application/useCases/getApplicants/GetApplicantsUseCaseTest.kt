package org.example.notifier.application.useCases.getApplicants

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.model.shared.PageQuery
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.applicant.CandidatePositionKey
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.shared.SystemConstants
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetApplicantsUseCaseTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var invitationService: InvitationService
    private lateinit var logger: LoggerPort
    private lateinit var useCase: GetApplicantsUseCase

    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        applicantService = mock(ApplicantService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        invitationService = mock(InvitationService::class.java)
        logger = mock(LoggerPort::class.java)
        useCase = GetApplicantsUseCase(applicantService, openPositionService, invitationService, logger)
        // Default: no system-level assessment invitations
        runBlocking {
            whenever(invitationService.findByRecruiterId(SystemConstants.SYSTEM_RECRUITER_ID)).thenReturn(emptyList())
        }
    }

    private fun buildApplicant(
        id: String = "app-1",
        positionId: String = "pos-1",
        email: String = "test@example.com",
        createdAt: LocalDateTime = now
    ) = Applicant(
            id = id, name = "Test User", email = email, phone = null,
            positionId = positionId, status = ApplicantStatus.PENDING,
            deleteAfter = now.plusMonths(9), createdAt = createdAt, updatedAt = now
        )

    private fun buildPosition(id: String = "pos-1", title: String = "Developer") =
        OpenPosition(id = id, title = title, description = "desc", createdBy = "admin-1", createdAt = now, updatedAt = now)

    private fun buildInvitation(
        candidateEmail: String = "test@example.com",
        positionId: String = "pos-1",
        recruiterId: String = "rec-1"
    ) = Invitation(
        candidateEmail = candidateEmail, candidateName = "Test User",
        assessmentId = "assess-1", openPositionId = positionId, recruiterId = recruiterId
    )

    // --- Admin path ---

    @Test
    fun `admin calls getAllApplicants with all filters`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = "PENDING", positionId = "pos-1", search = null)
        whenever(applicantService.getAllApplicants("PENDING", "pos-1", null)).thenReturn(emptyList())

        useCase.execute(command)

        verify(applicantService).getAllApplicants("PENDING", "pos-1", null)
    }

    @Test
    fun `admin does not call getApplicantsForRecruiter`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(emptyList())

        useCase.execute(command)

        verify(applicantService, never()).getApplicantsForRecruiter(any(), any(), any(), any())
    }

    @Test
    fun `enriches result with position title`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(listOf(buildApplicant()))
        whenever(openPositionService.getPosition("pos-1")).thenReturn(buildPosition(title = "Developer"))

        val result = useCase.execute(command)

        assertEquals(1, result.items.size)
        assertEquals("Developer", result.items[0].positionTitle)
    }

    @Test
    fun `position not found sets null title`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(listOf(buildApplicant()))
        whenever(openPositionService.getPosition("pos-1")).thenReturn(null)

        val result = useCase.execute(command)

        assertNull(result.items[0].positionTitle)
    }

    @Test
    fun `fetches assessment invitations from system recruiter id`() = runBlocking<Unit> {
        val applicant = buildApplicant(email = "test@example.com", positionId = "pos-1")
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        val systemInvitation = buildInvitation(
            candidateEmail = "test@example.com",
            positionId = "pos-1",
            recruiterId = SystemConstants.SYSTEM_RECRUITER_ID
        ).copy(finalScore = 90.0, qualified = true, status = "completed")

        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(listOf(applicant))
        whenever(openPositionService.getPosition("pos-1")).thenReturn(buildPosition())
        whenever(invitationService.findByRecruiterId(SystemConstants.SYSTEM_RECRUITER_ID))
            .thenReturn(listOf(systemInvitation))

        val result = useCase.execute(command)

        assertEquals(1, result.items.size)
        assertNotNull(result.items[0].assessments)
        assertEquals(1, result.items[0].assessments!!.size)
        assertEquals("assess-1", result.items[0].assessments!![0].assessmentId)
        assertEquals(90.0, result.items[0].assessments!![0].finalScore)
    }

    @Test
    fun `applicant with no matching system invitation has null assessments`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(listOf(buildApplicant()))
        whenever(openPositionService.getPosition(any())).thenReturn(buildPosition())

        val result = useCase.execute(command)

        assertNull(result.items[0].assessments)
    }

    @Test
    fun `empty applicant list returns empty result`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(emptyList())

        val result = useCase.execute(command)

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
    }

    // --- Recruiter path ---

    @Test
    fun `recruiter with no invitations returns empty list without calling service`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("rec-1", isAdmin = false, status = null, positionId = null, search = null)
        whenever(invitationService.findByRecruiterId("rec-1")).thenReturn(emptyList())

        val result = useCase.execute(command)

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
        verify(applicantService, never()).getApplicantsForRecruiter(any(), any(), any(), any())
    }

    @Test
    fun `recruiter calls getApplicantsForRecruiter with invitedPairs built from own invitations`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("rec-1", isAdmin = false, status = null, positionId = null, search = null)
        whenever(invitationService.findByRecruiterId("rec-1"))
            .thenReturn(listOf(buildInvitation(candidateEmail = "jane@example.com", positionId = "pos-1")))
        whenever(applicantService.getApplicantsForRecruiter(any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(emptyList())

        useCase.execute(command)

        verify(applicantService).getApplicantsForRecruiter(
            argThat { pairs: Set<CandidatePositionKey> -> pairs == setOf(CandidatePositionKey("jane@example.com", "pos-1")) },
            isNull(), isNull(), isNull()
        )
    }

    @Test
    fun `recruiter passes status positionId and search filters to service`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("rec-1", isAdmin = false, status = "PENDING", positionId = "pos-1", search = "jane")
        whenever(invitationService.findByRecruiterId("rec-1"))
            .thenReturn(listOf(buildInvitation()))
        whenever(applicantService.getApplicantsForRecruiter(any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(emptyList())

        useCase.execute(command)

        verify(applicantService).getApplicantsForRecruiter(any(), eq("PENDING"), eq("pos-1"), eq("jane"))
    }

    @Test
    fun `recruiter result is enriched with position title`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("rec-1", isAdmin = false, status = null, positionId = null, search = null)
        whenever(invitationService.findByRecruiterId("rec-1")).thenReturn(listOf(buildInvitation()))
        whenever(applicantService.getApplicantsForRecruiter(any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(listOf(buildApplicant()))
        whenever(openPositionService.getPosition("pos-1")).thenReturn(buildPosition(title = "Backend Engineer"))

        val result = useCase.execute(command)

        assertEquals(1, result.items.size)
        assertEquals("Backend Engineer", result.items[0].positionTitle)
    }

    @Test
    fun `recruiter does not call getAllApplicants`() = runBlocking<Unit> {
        val command = GetApplicantsCommand("rec-1", isAdmin = false, status = null, positionId = null, search = null)
        whenever(invitationService.findByRecruiterId("rec-1")).thenReturn(listOf(buildInvitation()))
        whenever(applicantService.getApplicantsForRecruiter(any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(emptyList())

        useCase.execute(command)

        verify(applicantService, never()).getAllApplicants(any(), any(), any())
    }

    @Test
    fun `paginates results correctly`() = runBlocking<Unit> {
        val applicants = listOf(
            buildApplicant(id = "app-1"),
            buildApplicant(id = "app-2"),
            buildApplicant(id = "app-3")
        )
        val command0 = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null, pageQuery = PageQuery(page = 0, pageSize = 2))
        val command1 = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null, pageQuery = PageQuery(page = 1, pageSize = 2))
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(applicants)
        whenever(openPositionService.getPosition(any())).thenReturn(buildPosition())

        val page0 = useCase.execute(command0)
        val page1 = useCase.execute(command1)

        assertEquals(3, page0.total)
        assertEquals(2, page0.items.size)
        assertEquals(3, page1.total)
        assertEquals(1, page1.items.size)
    }

    @Test
    fun `returns applicants sorted by createdAt descending`() = runBlocking<Unit> {
        val older = buildApplicant(id = "old", email = "old@example.com", createdAt = now.minusDays(1))
        val newer = buildApplicant(id = "new", email = "new@example.com", createdAt = now)
        val command = GetApplicantsCommand("admin-1", isAdmin = true, status = null, positionId = null, search = null)
        whenever(applicantService.getAllApplicants(null, null, null)).thenReturn(listOf(older, newer))
        whenever(openPositionService.getPosition(any())).thenReturn(buildPosition())

        val result = useCase.execute(command)

        assertEquals("new@example.com", result.items[0].email)
        assertEquals("old@example.com", result.items[1].email)
    }
}