package org.example.notifier.application.useCases.getCandidates

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.model.shared.PageQuery
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.shared.SystemConstants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetCandidatesUseCaseTest {

    private lateinit var invitationService: InvitationService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetCandidatesUseCase

    private val positionId = "pos-1"
    private val recruiterId = "rec-1"
    private val now = LocalDateTime.now()
    private val position = OpenPosition(id = positionId, title = "Engineer", description = "", createdBy = "admin-1")

    @BeforeEach
    fun setup() {
        invitationService = mock(InvitationService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        useCase = GetCandidatesUseCase(invitationService, openPositionService)
    }

    @Test
    fun `execute with null recruiterId returns all non-system invitations`() = runBlocking<Unit> {
        val inv = buildInvitation("a@example.com", recruiterId)
        val systemInv = buildInvitation("b@example.com", SystemConstants.SYSTEM_RECRUITER_ID)
        whenever(invitationService.findAll()).thenReturn(listOf(inv, systemInv))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = null))

        assertEquals(1, result.items.size)
        assertEquals(1, result.total)
        assertEquals("a@example.com", result.items[0].candidateEmail)
    }

    @Test
    fun `execute with recruiterId returns only that recruiter invitations`() = runBlocking<Unit> {
        val inv = buildInvitation("a@example.com", recruiterId)
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        assertEquals(1, result.items.size)
        assertEquals(recruiterId, result.items[0].recruiterId)
    }

    @Test
    fun `execute groups multiple assessments for same candidate and position into one item`() = runBlocking<Unit> {
        val inv1 = buildInvitation("a@example.com", recruiterId, assessmentId = "a1")
        val inv2 = buildInvitation("a@example.com", recruiterId, assessmentId = "a2")
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv1, inv2))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        assertEquals(1, result.items.size)
        assertEquals(2, result.items[0].assessments.size)
    }

    @Test
    fun `execute maps positionTitle from openPositionService`() = runBlocking<Unit> {
        val inv = buildInvitation("a@example.com", recruiterId)
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        assertEquals("Engineer", result.items[0].positionTitle)
    }

    @Test
    fun `execute sets positionTitle to null when position not found`() = runBlocking<Unit> {
        val inv = buildInvitation("a@example.com", recruiterId)
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv))
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        assertEquals(null, result.items[0].positionTitle)
    }

    @Test
    fun `execute returns empty list when no invitations found`() = runBlocking<Unit> {
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(emptyList())

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
    }

    @Test
    fun `execute sorts results by invitedAt descending`() = runBlocking<Unit> {
        val older = buildInvitation("old@example.com", recruiterId, createdAt = now.minusDays(1))
        val newer = buildInvitation("new@example.com", recruiterId, createdAt = now)
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(older, newer))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        assertEquals("new@example.com", result.items[0].candidateEmail)
        assertEquals("old@example.com", result.items[1].candidateEmail)
    }

    @Test
    fun `execute maps all assessment fields correctly`() = runBlocking<Unit> {
        val inv = buildInvitation("a@example.com", recruiterId).copy(
            assessmentName = "Java Test",
            status = "completed",
            finalScore = 85.0,
            mcScore = 90,
            codeScore = 80,
            qualified = true
        )
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId))

        with(result.items[0].assessments[0]) {
            assertEquals("Java Test", assessmentName)
            assertEquals("completed", status)
            assertEquals(85.0, finalScore)
            assertEquals(90, mcScore)
            assertEquals(80, codeScore)
            assertEquals(true, qualified)
        }
    }

    @Test
    fun `execute paginates results correctly`() = runBlocking<Unit> {
        val invitations = listOf(
            buildInvitation("a@example.com", recruiterId, createdAt = now.minusDays(2)),
            buildInvitation("b@example.com", recruiterId, createdAt = now.minusDays(1)),
            buildInvitation("c@example.com", recruiterId, createdAt = now)
        )
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(invitations)
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val page0 = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId, pageQuery = PageQuery(page = 0, pageSize = 2)))
        val page1 = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId, pageQuery = PageQuery(page = 1, pageSize = 2)))

        assertEquals(3, page0.total)
        assertEquals(2, page0.items.size)
        assertEquals(3, page1.total)
        assertEquals(1, page1.items.size)
    }

    @Test
    fun `search matches positionTitle even when name and email do not match`() = runBlocking<Unit> {
        val inv = buildInvitation("dev@example.com", recruiterId)
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv))
        whenever(openPositionService.getPosition(positionId))
            .thenReturn(OpenPosition(id = positionId, title = "Backend Engineer", description = "", createdBy = "admin-1"))

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId, search = "backend"))

        assertEquals(1, result.items.size)
        assertEquals("Backend Engineer", result.items[0].positionTitle)
    }

    @Test
    fun `search by name still works after position title search refactor`() = runBlocking<Unit> {
        val inv = buildInvitation("alice@example.com", recruiterId)
        whenever(invitationService.findByRecruiterId(recruiterId)).thenReturn(listOf(inv))
        whenever(openPositionService.getPosition(positionId)).thenReturn(position)

        val result = useCase.execute(GetCandidatesCommand(recruiterId = recruiterId, search = "Test User"))

        assertEquals(1, result.items.size)
    }

    private fun buildInvitation(
        email: String,
        recruiterId: String,
        assessmentId: String = "assess-1",
        createdAt: LocalDateTime = now
    ) = Invitation(
        candidateEmail = email,
        candidateName = "Test User",
        assessmentId = assessmentId,
        openPositionId = positionId,
        recruiterId = recruiterId,
        status = "invited",
        createdAt = createdAt
    )
}