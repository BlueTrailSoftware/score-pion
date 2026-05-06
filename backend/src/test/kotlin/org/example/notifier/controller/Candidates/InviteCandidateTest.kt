package org.example.notifier.controller.Candidates

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getCandidates.GetCandidatesUseCase
import org.example.notifier.application.useCases.inviteCandidate.InviteCandidateCommand
import org.example.notifier.application.useCases.inviteCandidate.InviteCandidateUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.controller.CandidatesController
import org.example.notifier.infrastructure.dto.request.InviteCandidateRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class InviteCandidateTest {

    private lateinit var getCandidatesUseCase: GetCandidatesUseCase
    private lateinit var inviteCandidateUseCase: InviteCandidateUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: CandidatesController

    private val responseFactory = ResponseEntityFactory()
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
    fun `inviteCandidate returns 204 on success`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn("rec-1")

        val request = InviteCandidateRequest(
            email = "cand@example.com",
            candidateName = "John Doe",
            positionId = "pos-1"
        )

        val response = controller.inviteCandidate(request)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `inviteCandidate passes correct command to use case`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn("rec-1")

        val request = InviteCandidateRequest(
            email = "cand@example.com",
            candidateName = "John Doe",
            positionId = "pos-1"
        )

        controller.inviteCandidate(request)

        verify(inviteCandidateUseCase).execute(
            InviteCandidateCommand(
                candidateEmail = "cand@example.com",
                candidateName = "John Doe",
                positionId = "pos-1",
                recruiterId = "rec-1"
            )
        )
    }

    @Test
    fun `inviteCandidate returns 400 with message on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn("rec-1")
        whenever(
            inviteCandidateUseCase.execute(
                InviteCandidateCommand("cand@example.com", "John Doe", "pos-no-assessments", "rec-1")
            )
        ).thenThrow(IllegalArgumentException("Position has no assessments assigned"))

        val request = InviteCandidateRequest("cand@example.com", "John Doe", "pos-no-assessments")

        val response = controller.inviteCandidate(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Position has no assessments assigned", response.body?.message)
    }

    @Test
    fun `inviteCandidate returns 400 with message when candidate already self-applied`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn("rec-1")
        whenever(
            inviteCandidateUseCase.execute(
                InviteCandidateCommand("cand@example.com", "John Doe", "pos-1", "rec-1")
            )
        ).thenThrow(IllegalArgumentException("This candidate has already applied to this position on their own"))

        val request = InviteCandidateRequest("cand@example.com", "John Doe", "pos-1")

        val response = controller.inviteCandidate(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("This candidate has already applied to this position on their own", response.body?.message)
    }

    @Test
    fun `inviteCandidate returns 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn("rec-1")
        whenever(
            inviteCandidateUseCase.execute(
                InviteCandidateCommand("cand@example.com", "John Doe", "pos-1", "rec-1")
            )
        ).thenThrow(RuntimeException("DB error"))

        val request = InviteCandidateRequest("cand@example.com", "John Doe", "pos-1")

        val response = controller.inviteCandidate(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Failed to invite candidate", response.body?.message)
    }
}