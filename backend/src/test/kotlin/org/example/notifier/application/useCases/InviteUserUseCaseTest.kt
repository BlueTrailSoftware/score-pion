package org.example.notifier.application.useCases.inviteUser

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.domain.event.UserInvitedEvent
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.assertEquals
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

class InviteUserUseCaseTest {

    private lateinit var recruiterInvitationService: RecruiterInvitationService
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var logger: LoggerPort
    private lateinit var useCase: InviteUserUseCase

    @BeforeEach
    fun setup() {
        recruiterInvitationService = mock(RecruiterInvitationService::class.java)
        eventPublisher = mock(ApplicationEventPublisher::class.java)
        logger = mock(LoggerPort::class.java)
        useCase = InviteUserUseCase(recruiterInvitationService, eventPublisher, logger)
    }

    @Test
    fun `execute should create invitation and send recruiter email for RECRUITER role`() = runBlocking<Unit> {
        val command = InviteUserCommand(
            email = "recruiter@example.com",
            role = UserRole.RECRUITER,
            positionIds = listOf("pos-1", "pos-2"),
            invitedBy = "admin-1",
            adminName = "Admin Name"
        )
        val savedInvitation = RecruiterInvitation(
            id = "inv-1",
            email = command.email,
            invitedBy = command.invitedBy,
            assignedPositions = command.positionIds!!,
            role = command.role
        )

        whenever(recruiterInvitationService.createInvitation(
            email = command.email,
            invitedBy = command.invitedBy,
            positionIds = command.positionIds,
            role = command.role
        )).thenReturn(savedInvitation)

        val result = useCase.execute(command)

        assertEquals(savedInvitation.id, result.id)
        assertEquals(savedInvitation.email, result.email)
        assertEquals(savedInvitation.assignedPositions, result.assignedPositions)
        assertEquals(savedInvitation.status, result.status)

        verify(eventPublisher).publishEvent(argThat<UserInvitedEvent> { event ->
            event is UserInvitedEvent
                && event.recipientEmail == command.email
                && event.role == UserRole.RECRUITER
                && event.adminName == command.adminName
        })
    }

    @Test
    fun `execute should send admin email for ADMIN role`() = runBlocking<Unit> {
        val command = InviteUserCommand(
            email = "admin@example.com",
            role = UserRole.ADMIN,
            invitedBy = "admin-1",
            adminName = "Admin Name"
        )
        val savedInvitation = RecruiterInvitation(
            id = "inv-2",
            email = command.email,
            invitedBy = command.invitedBy,
            assignedPositions = emptyList(),
            role = command.role
        )

        whenever(recruiterInvitationService.createInvitation(
            email = command.email,
            invitedBy = command.invitedBy,
            positionIds = null,
            role = command.role
        )).thenReturn(savedInvitation)

        val result = useCase.execute(command)

        assertEquals(savedInvitation.id, result.id)
        verify(eventPublisher).publishEvent(argThat<UserInvitedEvent> { event ->
            event is UserInvitedEvent
                && event.recipientEmail == "admin@example.com"
                && event.role == UserRole.ADMIN
        })
    }

    @Test
    fun `execute should propagate exception when email already exists`() = runBlocking<Unit> {
        val command = InviteUserCommand(
            email = "existing@example.com",
            role = UserRole.RECRUITER,
            invitedBy = "admin-1",
            adminName = "Admin Name"
        )

        whenever(recruiterInvitationService.createInvitation(any(), any(), anyOrNull(), anyOrNull()))
            .thenThrow(IllegalArgumentException("An invitation for this email already exists"))

        assertThrows<IllegalArgumentException> { useCase.execute(command) }

        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `execute should still return result when event publishing fails`() = runBlocking<Unit> {
        val command = InviteUserCommand(
            email = "recruiter@example.com",
            role = UserRole.RECRUITER,
            invitedBy = "admin-1",
            adminName = "Admin Name"
        )
        val savedInvitation = RecruiterInvitation(
            id = "inv-3",
            email = command.email,
            invitedBy = command.invitedBy,
            role = command.role
        )

        whenever(recruiterInvitationService.createInvitation(any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(savedInvitation)
        whenever(eventPublisher.publishEvent(any())).thenThrow(RuntimeException("event bus down"))

        val result = useCase.execute(command)

        assertEquals(savedInvitation.id, result.id)
    }
}
