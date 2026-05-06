package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.UserRole
import org.example.notifier.domain.port.RecruiterInvitationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RecruiterInvitationServiceImplTest {

    private lateinit var recruiterInvitationRepository: RecruiterInvitationRepository
    private lateinit var service: RecruiterInvitationServiceImpl

    @BeforeEach
    fun setup() {
        recruiterInvitationRepository = mock(RecruiterInvitationRepository::class.java)
        service = RecruiterInvitationServiceImpl(recruiterInvitationRepository)
    }

    @Test
    fun `createInvitation should use provided role`() = runBlocking<Unit> {
        val email = "test@example.com"
        val invitedBy = "admin-1"
        val role = UserRole.ADMIN

        whenever(recruiterInvitationRepository.save(any())).thenAnswer { it.arguments[0] as RecruiterInvitation }

        val result = service.createInvitation(email, invitedBy, null, role)

        assert(result.role == role)
        verify(recruiterInvitationRepository).save(argThat { this.role == role })
    }

    @Test
    fun `getAllPendingInvitations should return only PENDING non-expired invitations`() = runBlocking<Unit> {
        val pending = RecruiterInvitation(
            email = "pending@example.com",
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.PENDING,
            expiresAt = java.time.LocalDateTime.now().plusDays(1)
        )
        val accepted = RecruiterInvitation(
            email = "accepted@example.com",
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.ACCEPTED
        )
        val expired = RecruiterInvitation(
            email = "expired@example.com",
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.PENDING,
            expiresAt = java.time.LocalDateTime.now().minusDays(1)
        )

        whenever(recruiterInvitationRepository.findAll()).thenReturn(listOf(pending, accepted, expired))

        val result = service.getAllPendingInvitations()

        assertEquals(1, result.size)
        assertEquals("pending@example.com", result[0].email)
    }

    @Test
    fun `getAllPendingInvitations should return empty list when all invitations are accepted or expired`() = runBlocking<Unit> {
        val accepted = RecruiterInvitation(
            email = "accepted@example.com",
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.ACCEPTED
        )
        val revoked = RecruiterInvitation(
            email = "revoked@example.com",
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.REVOKED
        )

        whenever(recruiterInvitationRepository.findAll()).thenReturn(listOf(accepted, revoked))

        val result = service.getAllPendingInvitations()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllPendingInvitations should return empty list when repository is empty`() = runBlocking<Unit> {
        whenever(recruiterInvitationRepository.findAll()).thenReturn(emptyList())

        val result = service.getAllPendingInvitations()

        assertTrue(result.isEmpty())
    }
}