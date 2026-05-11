package org.example.notifier.infrastructure.security

import kotlinx.coroutines.reactive.awaitSingle
import org.example.notifier.domain.user.User
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityUtils {

    /**
     * Get current authenticated user (full User object) from reactive context
     * This avoids having to do userService.findById() in every controller
     */
    suspend fun getCurrentUser(): User {
        val authentication = ReactiveSecurityContextHolder.getContext()
            .awaitSingle()
            .authentication

        val principal = authentication?.principal as? User
            ?: throw IllegalStateException("User not authenticated")

        return principal
    }

    /**
     * Get current authenticated user ID
     */
    suspend fun getCurrentUserId(): String {
        return getCurrentUser().id
    }

    /**
     * Get current authenticated user email
     */
    suspend fun getCurrentUserEmail(): String {
        return getCurrentUser().email
    }
}
