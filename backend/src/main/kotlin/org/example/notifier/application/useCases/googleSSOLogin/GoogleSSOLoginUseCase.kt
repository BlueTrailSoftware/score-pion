package org.example.notifier.application.useCases.googleSSOLogin

import java.time.LocalDateTime
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.integration.GoogleOAuthService
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.domain.event.RecruiterCreatedEvent
import org.example.notifier.infrastructure.external.GoogleUserInfo
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class GoogleSSOLoginUseCase(
    private val googleOAuthService: GoogleOAuthService,
    private val userService: UserService,
    private val recruiterInvitationService: RecruiterInvitationService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val logger: LoggerPort,
    @Value("\${admin.email:}") private val adminEmail: String
) {

    suspend fun execute(command: GoogleSSOLoginCommand): GoogleSSOLoginResult {
        logger.info("Processing Google SSO login — credential present: true")

        val payload = googleOAuthService.verifyToken(command.credential)
            ?: return GoogleSSOLoginResult.InvalidCredential

        val googleUserInfo = googleOAuthService.extractUserInfo(payload)

        if (!googleUserInfo.emailVerified) {
            return GoogleSSOLoginResult.EmailNotVerified
        }

        val user = createOrUpdateUser(googleUserInfo)
            ?: return GoogleSSOLoginResult.AccessDenied

        if (!user.isActive) {
            return GoogleSSOLoginResult.AccountDeactivated
        }

        val token = userService.generateAuthToken(user)
        return GoogleSSOLoginResult.Success(userId = user.id, authToken = token, role = user.role)
    }

    private suspend fun createOrUpdateUser(googleUserInfo: GoogleUserInfo): User? {
        var existingUser = userService.findByGoogleId(googleUserInfo.googleId)
        if (existingUser == null) {
            existingUser = userService.findByEmail(googleUserInfo.email)
        }

        return if (existingUser != null) {
            var updatedUser = existingUser.copy(
                googleId = googleUserInfo.googleId,
                email = googleUserInfo.email,
                name = googleUserInfo.name,
                pictureUrl = googleUserInfo.pictureUrl,
                updatedAt = LocalDateTime.now()
            )
            val invitation = recruiterInvitationService.findByEmail(googleUserInfo.email)
            if (invitation != null && invitation.isValid() && invitation.role == UserRole.ADMIN) {
                recruiterInvitationService.acceptInvitation(invitation)
                updatedUser = updatedUser.copy(role = UserRole.ADMIN)
            }
            userService.save(updatedUser)
        } else {
            val isAdmin = adminEmail.isNotBlank() && googleUserInfo.email == adminEmail
            if (isAdmin) {
                val newAdmin = User(
                    email = googleUserInfo.email,
                    name = googleUserInfo.name,
                    googleId = googleUserInfo.googleId,
                    pictureUrl = googleUserInfo.pictureUrl,
                    role = UserRole.ADMIN
                )
                userService.save(newAdmin)
            } else {
                val invitation = recruiterInvitationService.findByEmail(googleUserInfo.email)
                if (invitation != null && (invitation.isValid() || invitation.status == "ACCEPTED")) {
                    val newUser = User(
                        email = googleUserInfo.email,
                        name = googleUserInfo.name,
                        googleId = googleUserInfo.googleId,
                        pictureUrl = googleUserInfo.pictureUrl,
                        role = invitation.role
                    )
                    val savedUser = userService.save(newUser)
                    if (invitation.isValid()) {
                        recruiterInvitationService.acceptInvitation(invitation)
                    }
                    applicationEventPublisher.publishEvent(RecruiterCreatedEvent(savedUser, invitation))
                    savedUser
                } else {
                    null
                }
            }
        }
    }
}
