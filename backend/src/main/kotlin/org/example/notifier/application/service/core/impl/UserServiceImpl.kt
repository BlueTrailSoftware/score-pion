package org.example.notifier.application.service.core.impl

import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.domain.event.RecruiterCreatedEvent
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.external.Auth0UserInfo
import org.example.notifier.infrastructure.external.GoogleUserInfo
import org.example.notifier.domain.port.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.security.AuthTokenService
import java.time.LocalDateTime

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val authTokenService: AuthTokenService,
    private val recruiterInvitationService: RecruiterInvitationService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @Value("\${admin.email:}")
    private val adminEmail: String
) : UserService {

    /**
     * Find user by ID
     */
    override suspend fun findById(id: String): User? {
        return userRepository.findById(id)
    }

    /**
     * Find user by email
     */
    override suspend fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    /**
     * Find user by Google ID
     */
    override suspend fun findByGoogleId(googleId: String): User? {
        return userRepository.findByGoogleId(googleId)
    }

    override suspend fun save(user: User): User {
        return userRepository.save(user)
    }

    /**
     * Create or update user from Google OAuth
     * Only allows login for users who:
     *   1. Already exist in the database (returning users)
     *   2. Have a valid invitation (new recruiters)
     *   3. Are the configured admin (admin.email property)
     */
    override suspend fun createOrUpdateFromGoogle(googleUserInfo: GoogleUserInfo): User? {
        var existingUser = findByGoogleId(googleUserInfo.googleId)

        // If not found by googleId, try to find by email (handles manually created users)
        if (existingUser == null) {
            existingUser = userRepository.findByEmail(googleUserInfo.email)
        }

        return if (existingUser != null) {
            // User already exists - update their information and associate googleId if needed
            var updatedUser = existingUser.copy(
                googleId = googleUserInfo.googleId,
                email = googleUserInfo.email,
                name = googleUserInfo.name,
                pictureUrl = googleUserInfo.pictureUrl,
                updatedAt = LocalDateTime.now()
            )

            // Fix Pending Invitation Leak (Admins Only)
            val invitation = recruiterInvitationService.findByEmail(googleUserInfo.email)
            if (invitation != null && invitation.isValid() && invitation.role == UserRole.ADMIN) {
                recruiterInvitationService.acceptInvitation(invitation)
                updatedUser = updatedUser.copy(role = UserRole.ADMIN)
            }

            userRepository.save(updatedUser)
        } else {
            // User does not exist - check if they have permission to register
            val isAdmin = adminEmail.isNotBlank() && googleUserInfo.email == adminEmail

            if (isAdmin) {
                // Create admin user (no invitation required)
                val newAdmin = User(
                    email = googleUserInfo.email,
                    name = googleUserInfo.name,
                    googleId = googleUserInfo.googleId,
                    pictureUrl = googleUserInfo.pictureUrl,
                    role = UserRole.ADMIN
                )
                userRepository.save(newAdmin)
            } else {
                // Check if user has a valid or accepted invitation
                val invitation = recruiterInvitationService.findByEmail(googleUserInfo.email)

                if (invitation != null && (invitation.isValid() || invitation.status == "ACCEPTED")) {
                    // User has valid invitation - create user account with specified role
                    val newUser = User(
                        email = googleUserInfo.email,
                        name = googleUserInfo.name,
                        googleId = googleUserInfo.googleId,
                        pictureUrl = googleUserInfo.pictureUrl,
                        role = invitation.role
                    )

                    val savedUser = userRepository.save(newUser)

                    // Mark invitation as accepted if it was pending
                    if (invitation.isValid()) {
                        recruiterInvitationService.acceptInvitation(invitation)
                    }

                    // Publish event to copy assessments from invitation
                    applicationEventPublisher.publishEvent(
                        RecruiterCreatedEvent(savedUser, invitation)
                    )

                    savedUser
                } else {
                    // No valid invitation - reject login
                    null
                }
            }
        }
    }

    /**
     * Generate authentication token for user
     */
    override fun generateAuthToken(user: User): String {
        return authTokenService.generateToken(
            userId = user.id,
            email = user.email,
            role = user.role
        )
    }

    /**
     * Validate JWT token
     */
    override fun validateToken(token: String): Boolean {
        return authTokenService.validateToken(token)
    }

    /**
     * Get user from token
     */
    override suspend fun getUserFromToken(token: String): User? {
        val userId = authTokenService.getUserIdFromToken(token) ?: return null
        return findById(userId)
    }

    /**
     * Find all users by role
     */
    override suspend fun findAllByRole(role: String): List<User> {
        return userRepository.findAllByRole(role)
    }

    /**
     * Update user active status
     */
    override suspend fun updateActiveStatus(userId: String, isActive: Boolean): User? {
        val user = findById(userId) ?: return null
        val updatedUser = user.copy(
            isActive = isActive,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    /**
     * Create or update user from Auth0
     * Only allows login for users who:
     *   1. Already exist in the database (returning users)
     *   2. Have a valid invitation (new recruiters)
     *   3. Are the configured admin (admin.email property)
     */
    override suspend fun createOrUpdateFromAuth0(auth0UserInfo: Auth0UserInfo): User? {
        // Look up by email instead of auth0Id (no GSI needed)
        var existingUser = userRepository.findByEmail(auth0UserInfo.email)

        return if (existingUser != null) {
            // User already exists - update their information and associate auth0Id if needed
            var updatedUser = existingUser.copy(
                auth0Id = auth0UserInfo.auth0Id,
                email = auth0UserInfo.email,
                name = auth0UserInfo.name,
                pictureUrl = auth0UserInfo.pictureUrl,
                updatedAt = LocalDateTime.now()
            )

            // Fix Pending Invitation Leak (Admins Only)
            val invitation = recruiterInvitationService.findByEmail(auth0UserInfo.email)
            if (invitation != null && invitation.isValid() && invitation.role == UserRole.ADMIN) {
                recruiterInvitationService.acceptInvitation(invitation)
                updatedUser = updatedUser.copy(role = UserRole.ADMIN)
            }

            userRepository.save(updatedUser)
        } else {
            // User does not exist - check if they have permission to register
            val isAdmin = adminEmail.isNotBlank() && auth0UserInfo.email == adminEmail

            if (isAdmin) {
                // Create admin user (no invitation required)
                val newAdmin = User(
                    email = auth0UserInfo.email,
                    name = auth0UserInfo.name,
                    auth0Id = auth0UserInfo.auth0Id,
                    pictureUrl = auth0UserInfo.pictureUrl,
                    role = UserRole.ADMIN
                )
                userRepository.save(newAdmin)
            } else {
                // Check if user has a valid or accepted invitation
                val invitation = recruiterInvitationService.findByEmail(auth0UserInfo.email)

                if (invitation != null && (invitation.isValid() || invitation.status == "ACCEPTED")) {
                    // User has valid invitation - create user account with specified role
                    val newUser = User(
                        email = auth0UserInfo.email,
                        name = auth0UserInfo.name,
                        auth0Id = auth0UserInfo.auth0Id,
                        pictureUrl = auth0UserInfo.pictureUrl,
                        role = invitation.role
                    )

                    val savedUser = userRepository.save(newUser)

                    // Mark invitation as accepted if it was pending
                    if (invitation.isValid()) {
                        recruiterInvitationService.acceptInvitation(invitation)
                    }

                    // Publish event to copy assessments from invitation
                    applicationEventPublisher.publishEvent(
                        RecruiterCreatedEvent(savedUser, invitation)
                    )

                    savedUser
                } else {
                    // No valid invitation - reject login
                    null
                }
            }
        }
    }
}
