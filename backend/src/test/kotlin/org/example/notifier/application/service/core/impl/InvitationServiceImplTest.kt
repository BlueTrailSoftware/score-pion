package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.port.InvitationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class InvitationServiceImplTest {

    private lateinit var invitationRepository: InvitationRepository
    private lateinit var openPositionService: OpenPositionService
    private lateinit var service: InvitationServiceImpl

    private val positionId = "pos-1"
    private val recruiterId = "rec-1"
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setup() {
        invitationRepository = mock(InvitationRepository::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        service = InvitationServiceImpl(invitationRepository, openPositionService)
    }

    // --- findAll ---

    @Test
    fun `findAll returns all invitations from repository`() = runBlocking<Unit> {
        val invitations = listOf(buildInvitation("a@example.com"), buildInvitation("b@example.com"))
        whenever(invitationRepository.findAll()).thenReturn(invitations)

        val result = service.findAll()

        assertEquals(2, result.size)
        assertEquals("a@example.com", result[0].candidateEmail)
    }

    @Test
    fun `findAll returns empty list when repository has no invitations`() = runBlocking<Unit> {
        whenever(invitationRepository.findAll()).thenReturn(emptyList())

        val result = service.findAll()

        assertTrue(result.isEmpty())
    }

    // --- findByRecruiterId ---

    @Test
    fun `findByRecruiterId returns invitations for the given recruiter`() = runBlocking<Unit> {
        val invitations = listOf(buildInvitation("a@example.com", recruiterId = recruiterId))
        whenever(invitationRepository.findByRecruiterId(recruiterId)).thenReturn(invitations)

        val result = service.findByRecruiterId(recruiterId)

        assertEquals(1, result.size)
        assertEquals(recruiterId, result[0].recruiterId)
    }

    @Test
    fun `findByRecruiterId returns empty list when recruiter has no invitations`() = runBlocking<Unit> {
        whenever(invitationRepository.findByRecruiterId(recruiterId)).thenReturn(emptyList())

        val result = service.findByRecruiterId(recruiterId)

        assertTrue(result.isEmpty())
    }

    // --- existsForCandidateAndPosition ---

    @Test
    fun `existsForCandidateAndPosition returns true when invitation exists for one assessment`() = runBlocking<Unit> {
        val assessments = listOf(
            buildPositionAssessment("a-1"),
            buildPositionAssessment("a-2")
        )
        whenever(openPositionService.getPositionAssessments(positionId)).thenReturn(assessments)
        whenever(invitationRepository.findByCandidateEmailAndAssessmentId("cand@example.com", "a-1"))
            .thenReturn(null)
        whenever(invitationRepository.findByCandidateEmailAndAssessmentId("cand@example.com", "a-2"))
            .thenReturn(buildInvitation("cand@example.com", assessmentId = "a-2"))

        val result = service.existsForCandidateAndPosition("cand@example.com", positionId)

        assertTrue(result)
    }

    @Test
    fun `existsForCandidateAndPosition returns false when no invitation exists for any assessment`() = runBlocking<Unit> {
        val assessments = listOf(
            buildPositionAssessment("a-1"),
            buildPositionAssessment("a-2")
        )
        whenever(openPositionService.getPositionAssessments(positionId)).thenReturn(assessments)
        whenever(invitationRepository.findByCandidateEmailAndAssessmentId("cand@example.com", "a-1"))
            .thenReturn(null)
        whenever(invitationRepository.findByCandidateEmailAndAssessmentId("cand@example.com", "a-2"))
            .thenReturn(null)

        val result = service.existsForCandidateAndPosition("cand@example.com", positionId)

        assertFalse(result)
    }

    @Test
    fun `existsForCandidateAndPosition returns false when position has no assessments`() = runBlocking<Unit> {
        whenever(openPositionService.getPositionAssessments(positionId)).thenReturn(emptyList())

        val result = service.existsForCandidateAndPosition("cand@example.com", positionId)

        assertFalse(result)
    }

    private fun buildInvitation(
        email: String,
        recruiterId: String = "rec-1",
        assessmentId: String = "assess-1"
    ) = Invitation(
        candidateEmail = email,
        candidateName = "Test Candidate",
        assessmentId = assessmentId,
        openPositionId = positionId,
        recruiterId = recruiterId,
        status = "invited",
        createdAt = now
    )

    private fun buildPositionAssessment(assessmentId: String) =
        OpenPositionAssessment(
            openPositionId = positionId,
            assessmentId = assessmentId,
            assessmentName = "Test Assessment"
        )
}
