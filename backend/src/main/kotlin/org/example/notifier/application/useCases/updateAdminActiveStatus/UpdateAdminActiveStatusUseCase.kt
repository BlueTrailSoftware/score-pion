package org.example.notifier.application.useCases.updateAdminActiveStatus

import org.example.notifier.application.service.core.UserService
import org.example.notifier.domain.user.UserRole
import org.springframework.stereotype.Component

@Component
class UpdateAdminActiveStatusUseCase(
    private val userService: UserService
) {

    suspend fun execute(command: UpdateAdminActiveStatusCommand): UpdateAdminActiveStatusResult {
        val user = userService.findById(command.adminId)
            ?: throw IllegalArgumentException("User not found")

        if (user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("User is not an admin")
        }

        val updated = userService.updateActiveStatus(command.adminId, command.isActive)
            ?: throw IllegalArgumentException("Failed to update status")

        return UpdateAdminActiveStatusResult(
            id = updated.id,
            email = updated.email,
            name = updated.name,
            role = updated.role,
            isActive = updated.isActive,
            createdAt = updated.createdAt
        )
    }
}
