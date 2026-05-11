package org.example.notifier.application.useCases.validateToken

import org.example.notifier.application.model.user.ValidateTokenResult
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.security.AuthTokenService
import org.springframework.stereotype.Component

@Component
class ValidateTokenUseCase(
    private val authTokenService: AuthTokenService,
    private val userService: UserService
) {
    suspend fun execute(authHeader: String): ValidateTokenResult {
        val token = authTokenService.extractTokenFromHeader(authHeader)
            ?: return ValidateTokenResult(valid = false, message = "No token provided")

        val userId = authTokenService.validateAndExtractUserId(token)
            ?: return ValidateTokenResult(valid = false, message = "Invalid token")

        val user = userService.findById(userId)
        return if (user != null && user.isActive) {
            ValidateTokenResult(valid = true, message = "Token is valid")
        } else {
            ValidateTokenResult(valid = false, message = "User not found or inactive")
        }
    }
}
