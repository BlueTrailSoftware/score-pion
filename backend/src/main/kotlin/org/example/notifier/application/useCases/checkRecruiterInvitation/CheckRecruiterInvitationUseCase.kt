package org.example.notifier.application.useCases.checkRecruiterInvitation

import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CheckRecruiterInvitationUseCase(
    private val recruiterInvitationService: RecruiterInvitationService,
    private val userService: UserService,
    private val logger: LoggerPort,
    @Value("\${admin.email:}") private val adminEmail: String
) {

    suspend fun execute(command: CheckRecruiterInvitationCommand): CheckRecruiterInvitationResult {
        val isAdmin = adminEmail.isNotBlank() && command.email.equals(adminEmail, ignoreCase = true)
        if (isAdmin) {
            logger.info("Auth0 pre-registration check: admin email allowed — {}", command.email)
            return CheckRecruiterInvitationResult(allowed = true)
        }

        val existingUser = userService.findByEmail(command.email)
        val invitation = recruiterInvitationService.findByEmail(command.email)
        val allowed = existingUser != null || (invitation != null && invitation.isValid())

        logger.info(
            "Auth0 pre-registration check for {}: existingUser={}, allowed={}",
            command.email,
            existingUser != null,
            allowed
        )

        return if (allowed) {
            CheckRecruiterInvitationResult(allowed = true)
        } else {
            CheckRecruiterInvitationResult(allowed = false, reason = "No valid invitation found")
        }
    }
}
