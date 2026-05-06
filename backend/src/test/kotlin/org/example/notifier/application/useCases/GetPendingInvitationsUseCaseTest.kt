package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetPendingInvitationsUseCaseTest {

    private lateinit var recruiterInvitationService: RecruiterInvitationService
    private lateinit var useCase: GetPendingInvitationsUseCase

    private val now = LocalDateTime.now()

    private val invitation = RecruiterInvitation(
        id = "inv-1",
        email = "recruiter@example.com",
        invitedBy = "admin-1",
        assignedPositions = listOf("pos-1", "pos-2"),
        status = RecruiterInvitation.InvitationStatus.PENDING,
        createdAt = now,
        expiresAt = now.plusDays(3),
        acceptedAt = null
    )

    @BeforeEach
    fun setup() {
        recruiterInvitationService = mock(RecruiterInvitationService::class.java)
        useCase = GetPendingInvitationsUseCase(recruiterInvitationService)
    }

    @Test
    fun `execute should map RecruiterInvitation fields to InvitationItem`() = runBlocking<Unit> {
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(listOf(invitation))

        val result = useCase.execute()

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals("inv-1", id)
            assertEquals("recruiter@example.com", email)
            assertEquals("admin-1", invitedBy)
            assertEquals(listOf("pos-1", "pos-2"), assignedPositions)
            assertEquals(RecruiterInvitation.InvitationStatus.PENDING, status)
            assertEquals(now, createdAt)
            assertEquals(now.plusDays(3), expiresAt)
            assertNull(acceptedAt)
        }
    }

    @Test
    fun `execute should return all invitations from service`() = runBlocking<Unit> {
        val invitations = listOf(
            invitation,
            invitation.copy(id = "inv-2", email = "other@example.com"),
            invitation.copy(id = "inv-3", email = "third@example.com")
        )
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(invitations)

        val result = useCase.execute()

        assertEquals(3, result.size)
    }

    @Test
    fun `execute should return empty list when no pending invitations`() = runBlocking<Unit> {
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val result = useCase.execute()

        assertTrue(result.isEmpty())
    }
}
