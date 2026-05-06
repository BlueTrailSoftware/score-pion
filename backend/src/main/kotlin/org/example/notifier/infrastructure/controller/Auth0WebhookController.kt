package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.checkRecruiterInvitation.CheckRecruiterInvitationCommand
import org.example.notifier.application.useCases.checkRecruiterInvitation.CheckRecruiterInvitationUseCase
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Value

/**
 * Endpoint called by Auth0 Pre-User-Registration Action to check
 * whether a given email is allowed to register (i.e. has a valid invitation
 * or is the configured admin email).
 *
 * Auth0 Action must send the shared secret in the X-Auth0-Secret header.
 */
@RestController
@RequestMapping("/auth0")
class Auth0WebhookController(
    private val checkRecruiterInvitationUseCase: CheckRecruiterInvitationUseCase,
    @Value("\${auth0.webhook-secret:}")
    private val webhookSecret: String,
    private val logger: LoggerPort
) {

    @GetMapping("/check-invitation")
    suspend fun checkInvitation(
        @RequestParam("email") email: String,
        @RequestHeader("x-auth0-secret", required = false) secret: String?
    ): ResponseEntity<Map<String, Any>> {
        if (webhookSecret.isBlank() || secret != webhookSecret) {
            logger.warn("Auth0 check-invitation called with invalid or missing secret for email: $email")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("allowed" to false, "reason" to "Unauthorized"))
        }

        val result = checkRecruiterInvitationUseCase.execute(CheckRecruiterInvitationCommand(email))

        return if (result.allowed) {
            ResponseEntity.ok(mapOf("allowed" to true))
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("allowed" to false, "reason" to (result.reason ?: "No valid invitation found")))
        }
    }
}
