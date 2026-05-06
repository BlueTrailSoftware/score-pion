package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.getUserProfile.GetUserProfileUseCase
import org.example.notifier.application.useCases.validateToken.ValidateTokenUseCase
import org.example.notifier.infrastructure.dto.mapper.toResponse
import org.example.notifier.infrastructure.dto.response.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Users", description = "User profile, token validation and logout")
@RestController
@RequestMapping("/users")
class UserController(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val securityUtils: SecurityUtils,
    private val logger: LoggerPort,
    private val responseFactory: ResponseEntityFactory
) {

    @Operation(summary = "Get user profile by ID")
    @GetMapping("/{id}/profile")
    suspend fun getUserProfile(@PathVariable id: String): ResponseEntity<ApiResponse<UserResponse>> {
        val currentUser = securityUtils.getCurrentUser()
        if (currentUser.id != id && !currentUser.isAdmin()) {
            logger.warn("User ${currentUser.id} attempted to access profile of user $id")
            return responseFactory.forbidden("Access denied")
        }

        val result = getUserProfileUseCase.execute(id)
        if (result == null) {
            logger.warn("User not found with ID: $id")
            return responseFactory.notFound("User not found")
        }

        return responseFactory.success("User profile retrieved successfully", result.toResponse())
    }

    @Operation(summary = "Logout user (client-side JWT invalidation)")
    @DeleteMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<EmptyApiResponse>> {
        logger.info("User logout request")
        return responseFactory.success(
            "Logged out successfully",
            EmptyApiResponse(status = "success", message = "Logged out successfully")
        )
    }

    @Operation(summary = "Validate a JWT token and check user is active")
    @GetMapping("/token-validator")
    suspend fun validateToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<ApiResponse<EmptyApiResponse>> {
        logger.debug("Validating token")
        val result = validateTokenUseCase.execute(authHeader)
        return if (result.valid) {
            logger.debug("Token is valid")
            responseFactory.success("Token is valid", EmptyApiResponse(status = "success", message = "Token is valid"))
        } else {
            logger.warn(result.message)
            responseFactory.unauthorized(result.message)
        }
    }
}
