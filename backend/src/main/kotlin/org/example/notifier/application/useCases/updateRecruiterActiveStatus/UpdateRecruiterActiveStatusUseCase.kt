package org.example.notifier.application.useCases.updateRecruiterActiveStatus

import org.example.notifier.application.service.core.UserService
import org.springframework.stereotype.Component

@Component
class UpdateRecruiterActiveStatusUseCase(
    private val userService: UserService
) {

    suspend fun execute(command: UpdateRecruiterActiveStatusCommand): UpdateRecruiterActiveStatusResult {
        val user = userService.updateActiveStatus(command.recruiterId, command.isActive)
            ?: throw IllegalArgumentException("User not found")

        return UpdateRecruiterActiveStatusResult(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role,
            isActive = user.isActive,
            createdAt = user.createdAt
        )
    }
}
