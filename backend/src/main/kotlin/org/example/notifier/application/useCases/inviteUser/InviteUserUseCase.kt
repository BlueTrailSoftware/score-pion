package org.example.notifier.application.useCases.inviteUser

import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.domain.event.UserInvitedEvent
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class InviteUserUseCase(
    private val recruiterInvitationService: RecruiterInvitationService,
    private val eventPublisher: ApplicationEventPublisher,
    private val logger: LoggerPort
) {

    suspend fun execute(command: InviteUserCommand): InviteUserResult {
        val invitation = recruiterInvitationService.createInvitation(
            email = command.email,
            invitedBy = command.invitedBy,
            positionIds = command.positionIds,
            role = command.role
        )

        try {
            eventPublisher.publishEvent(
                UserInvitedEvent(
                    recipientEmail = invitation.email,
                    role = invitation.role,
                    adminName = command.adminName
                )
            )
        } catch (e: Exception) {
            logger.warn("Failed to publish invitation event for ${invitation.email}: ${e.message}")
        }

        return InviteUserResult(
            id = invitation.id,
            email = invitation.email,
            invitedBy = invitation.invitedBy,
            assignedPositions = invitation.assignedPositions,
            status = invitation.status,
            createdAt = invitation.createdAt,
            expiresAt = invitation.expiresAt,
            acceptedAt = invitation.acceptedAt
        )
    }
}
