package org.example.notifier.application.useCases.getRecruiters

import org.example.notifier.application.model.user.RecruiterListItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.model.shared.toPagedResult
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.domain.user.UserRole
import org.example.notifier.domain.user.UserStatus
import org.springframework.stereotype.Component

@Component
class GetRecruitersUseCase(
    private val userService: UserService,
    private val openPositionService: OpenPositionService,
    private val recruiterInvitationService: RecruiterInvitationService
) {

    suspend fun execute(command: GetRecruitersCommand = GetRecruitersCommand()): PagedResult<RecruiterListItem> {
        val activeRecruiters = userService.findAllByRole(UserRole.RECRUITER)
        val activeItems = activeRecruiters.map { recruiter ->
            val positions = openPositionService.getRecruiterPositions(recruiter.id)
            RecruiterListItem(
                id = recruiter.id,
                email = recruiter.email,
                name = recruiter.name,
                isActive = recruiter.isActive,
                status = if (recruiter.isActive) UserStatus.ACTIVE else UserStatus.INACTIVE,
                positionsCount = positions.size,
                createdAt = recruiter.createdAt
            )
        }

        val pendingInvitations = recruiterInvitationService.getAllPendingInvitations()
            .filter { it.role == UserRole.RECRUITER }

        val pendingItems = pendingInvitations.map { invitation ->
            RecruiterListItem(
                id = "",
                email = invitation.email,
                name = "",
                isActive = false,
                status = UserStatus.PENDING,
                positionsCount = invitation.assignedPositions.size,
                createdAt = invitation.createdAt
            )
        }

        return (activeItems + pendingItems)
            .sortedByDescending { it.createdAt }
            .toPagedResult(command.page, command.pageSize)
    }
}
