package org.example.notifier.application.useCases.getUserProfile

import org.example.notifier.application.model.user.UserProfileResult
import org.example.notifier.application.service.core.UserService
import org.springframework.stereotype.Component

@Component
class GetUserProfileUseCase(
    private val userService: UserService
) {
    suspend fun execute(userId: String): UserProfileResult? {
        val user = userService.findById(userId) ?: return null
        return UserProfileResult(
            id = user.id,
            email = user.email,
            name = user.name,
            pictureUrl = user.pictureUrl,
            role = user.role,
            isActive = user.isActive,
            createdAt = user.createdAt
        )
    }
}
