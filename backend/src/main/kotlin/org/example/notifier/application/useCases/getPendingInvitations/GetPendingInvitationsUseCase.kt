package org.example.notifier.application.useCases.getPendingInvitations

import org.example.notifier.application.model.invitation.InvitationItem
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.springframework.stereotype.Component

@Component
class GetPendingInvitationsUseCase(
    private val recruiterInvitationService: RecruiterInvitationService
) {

    suspend fun execute(): List<InvitationItem> {
        return recruiterInvitationService.getAllPendingInvitations().map { invitation ->
            InvitationItem(
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
}
