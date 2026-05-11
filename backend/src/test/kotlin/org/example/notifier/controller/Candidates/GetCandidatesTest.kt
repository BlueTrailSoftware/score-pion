package org.example.notifier.controller.Candidates

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.assessment.AssessmentInvitationItem
import org.example.notifier.application.model.applicant.CandidateItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.getCandidates.GetCandidatesCommand
import org.example.notifier.application.useCases.getCandidates.GetCandidatesUseCase
import org.example.notifier.application.useCases.inviteCandidate.InviteCandidateUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.controller.CandidatesController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class GetCandidatesTest {

    private lateinit var getCandidatesUseCase: GetCandidatesUseCase
    private lateinit var inviteCandidateUseCase: InviteCandidateUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: CandidatesController

    private val responseFactory = ResponseEntityFactory()
    private val now = LocalDateTime.now()

    private val adminUser = User(id = "admin-1", email = "admin@example.com", name = "Admin", role = "ADMIN")
    private val recruiterUser = User(id = "rec-1", email = "rec@example.com", name = "Recruiter", role = "RECRUITER")

    @BeforeEach
    fun setup() {
        getCandidatesUseCase = mock(GetCandidatesUseCase::class.java)
        inviteCandidateUseCase = mock(InviteCandidateUseCase::class.java)
        securityUtils = mock(SecurityUtils::class.java)
        logger = mock(LoggerPort::class.java)

        controller = CandidatesController(
            getCandidatesUseCase = getCandidatesUseCase,
            inviteCandidateUseCase = inviteCandidateUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger
        )
    }

    @Test
    fun `getCandidates returns 200 with list for ADMIN without recruiterId`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getCandidatesUseCase.execute(GetCandidatesCommand(recruiterId = null)))
            .thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getCandidates()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Candidates retrieved successfully", response.body?.message)
    }

    @Test
    fun `getCandidates ADMIN passes recruiterId filter to use case`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getCandidatesUseCase.execute(GetCandidatesCommand(recruiterId = "rec-99")))
            .thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getCandidates(recruiterId = "rec-99")

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `getCandidates RECRUITER always uses own id ignoring recruiterId param`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(recruiterUser)
        whenever(getCandidatesUseCase.execute(GetCandidatesCommand(recruiterId = "rec-1")))
            .thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getCandidates(recruiterId = "rec-other")

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `getCandidates maps CandidateItem fields to response`() = runBlocking {
        val item = CandidateItem(
            candidateEmail = "cand@example.com",
            candidateName = "Candidate Name",
            positionId = "pos-1",
            positionTitle = "Engineer",
            recruiterId = "rec-1",
            assessments = listOf(buildAssessmentItem()),
            invitedAt = now
        )
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getCandidatesUseCase.execute(GetCandidatesCommand(null))).thenReturn(PagedResult(listOf(item), 1))

        val response = controller.getCandidates()

        val data = response.body?.data
        assertNotNull(data)
        assertEquals(1, data!!.items.size)
        assertEquals(1, data.total)
        with(data.items[0]) {
            assertEquals("cand@example.com", candidateEmail)
            assertEquals("Candidate Name", candidateName)
            assertEquals("pos-1", positionId)
            assertEquals("Engineer", positionTitle)
            assertEquals("rec-1", recruiterId)
            assertEquals(1, assessments.size)
        }
    }

    @Test
    fun `getCandidates returns 500 on exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getCandidatesUseCase.execute(GetCandidatesCommand(null)))
            .thenThrow(RuntimeException("DB error"))

        val response = controller.getCandidates()

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }

    private fun buildAssessmentItem() = AssessmentInvitationItem(
        assessmentId = "a-1",
        assessmentName = "Java Test",
        status = "invited",
        finalScore = null,
        mcScore = null,
        codeScore = null,
        qualified = null,
        completedAt = null,
        plagiarism = null,
        pastedCode = null,
        suspiciousActivity = null,
        aiUsage = null,
        tabSwitchCount = null
    )
}