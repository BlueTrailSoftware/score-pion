package org.example.notifier.application.useCases.inviteUser

import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class InviteUserUseCase(
    private val recruiterInvitationService: RecruiterInvitationService,
    private val notificationOrchestrator: NotificationOrchestrator,
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
            if (invitation.role == UserRole.ADMIN) {
                notificationOrchestrator.notifyAdminInvitation(
                    recipientEmail = invitation.email,
                    invitedBy = command.adminName
                )
            } else {
                notificationOrchestrator.notifyRecruiterInvitation(
                    recipientEmail = invitation.email,
                    adminName = command.adminName
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to send invitation email to ${invitation.email}: ${e.message}")
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