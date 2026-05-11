package org.example.notifier.application.useCases.auth0Callback

import java.time.LocalDateTime
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.integration.Auth0Service
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.domain.event.RecruiterCreatedEvent
import org.example.notifier.infrastructure.external.Auth0UserInfo
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class Auth0CallbackUseCase(
    private val auth0Service: Auth0Service,
    private val userService: UserService,
    private val recruiterInvitationService: RecruiterInvitationService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val logger: LoggerPort,
    @Value("\${admin.email:}") private val adminEmail: String,
    @Value("\${auth0.require-email-verification:true}") private val requireEmailVerification: Boolean
) {

    suspend fun execute(command: Auth0CallbackCommand): Auth0CallbackResult {
        logger.info("Processing Auth0 callback — code present: true")

        val tokenResponse = auth0Service.exchangeCodeForTokens(command.code)
            ?: return Auth0CallbackResult.InvalidCode

        val decodedJwt = auth0Service.verifyIdToken(tokenResponse.id_token)
            ?: return Auth0CallbackResult.InvalidCode

        val auth0UserInfo = auth0Service.extractUserInfo(decodedJwt)
        logger.info(
            "Auth0 user info — email: {}, emailVerified: {}, name: {}",
            auth0UserInfo.email,
            auth0UserInfo.emailVerified,
            auth0UserInfo.name
        )

        if (requireEmailVerification && !auth0UserInfo.emailVerified) {
            logger.warn("Auth0 callback — email not verified for user: {}", auth0UserInfo.email)
            return Auth0CallbackResult.EmailNotVerified
        }

        val user = createOrUpdateUser(auth0UserInfo)
            ?: return Auth0CallbackResult.AccessDenied

        if (!user.isActive) {
            return Auth0CallbackResult.AccountDeactivated
        }

        val token = userService.generateAuthToken(user)
        return Auth0CallbackResult.Success(userId = user.id, authToken = token, role = user.role)
    }

    private suspend fun createOrUpdateUser(auth0UserInfo: Auth0UserInfo): User? {
        var existingUser = userService.findByEmail(auth0UserInfo.email)

        return if (existingUser != null) {
            var updatedUser = existingUser.copy(
                auth0Id = auth0UserInfo.auth0Id,
                email = auth0UserInfo.email,
                name = auth0UserInfo.name,
                pictureUrl = auth0UserInfo.pictureUrl,
                updatedAt = LocalDateTime.now()
            )
            val invitation = recruiterInvitationService.findByEmail(auth0UserInfo.email)
            if (invitation != null && invitation.isValid() && invitation.role == UserRole.ADMIN) {
                recruiterInvitationService.acceptInvitation(invitation)
                updatedUser = updatedUser.copy(role = UserRole.ADMIN)
            }
            userService.save(updatedUser)
        } else {
            val isAdmin = adminEmail.isNotBlank() && auth0UserInfo.email == adminEmail
            if (isAdmin) {
                val newAdmin = User(
                    email = auth0UserInfo.email,
                    name = auth0UserInfo.name,
                    auth0Id = auth0UserInfo.auth0Id,
                    pictureUrl = auth0UserInfo.pictureUrl,
                    role = UserRole.ADMIN
                )
                userService.save(newAdmin)
            } else {
                val invitation = recruiterInvitationService.findByEmail(auth0UserInfo.email)
                if (invitation != null && (invitation.isValid() || invitation.status == "ACCEPTED")) {
                    val newUser = User(
                        email = auth0UserInfo.email,
                        name = auth0UserInfo.name,
                        auth0Id = auth0UserInfo.auth0Id,
                        pictureUrl = auth0UserInfo.pictureUrl,
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
