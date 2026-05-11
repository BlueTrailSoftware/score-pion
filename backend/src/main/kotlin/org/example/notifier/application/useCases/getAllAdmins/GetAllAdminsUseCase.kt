package org.example.notifier.application.useCases.getAllAdmins

import org.example.notifier.application.model.user.AdminListItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.model.shared.toPagedResult
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.domain.user.UserRole
import org.example.notifier.domain.user.UserStatus
import org.springframework.stereotype.Component

@Component
class GetAllAdminsUseCase(
    private val userService: UserService,
    private val recruiterInvitationService: RecruiterInvitationService
) {

    suspend fun execute(command: GetAllAdminsCommand = GetAllAdminsCommand()): PagedResult<AdminListItem> {
        val activeAdmins = userService.findAllByRole(UserRole.ADMIN).map { admin ->
            AdminListItem(
                id = admin.id,
                email = admin.email,
                name = admin.name,
                isActive = admin.isActive,
                status = if (admin.isActive) UserStatus.ACTIVE else UserStatus.INACTIVE,
                createdAt = admin.createdAt
            )
        }

        val pendingAdmins = recruiterInvitationService.getAllPendingInvitations()
            .filter { it.role == UserRole.ADMIN }
            .map { invitation ->
                AdminListItem(
                    id = "",
                    email = invitation.email,
                    name = "",
                    isActive = false,
                    status = UserStatus.PENDING,
                    createdAt = invitation.createdAt
                )
            }

        return (activeAdmins + pendingAdmins)
            .sortedByDescending { it.createdAt }
            .toPagedResult(command.page, command.pageSize)
    }
}
