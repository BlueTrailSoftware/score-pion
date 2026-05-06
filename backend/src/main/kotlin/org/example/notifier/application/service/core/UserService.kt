package org.example.notifier.application.service.core

import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.external.Auth0UserInfo
import org.example.notifier.infrastructure.external.GoogleUserInfo

interface UserService {
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByGoogleId(googleId: String): User?
    suspend fun save(user: User): User
    @Deprecated("Use GoogleSSOLoginUseCase instead", ReplaceWith("GoogleSSOLoginUseCase.execute()"))
    suspend fun createOrUpdateFromGoogle(googleUserInfo: GoogleUserInfo): User?
    @Deprecated("Use Auth0CallbackUseCase instead", ReplaceWith("Auth0CallbackUseCase.execute()"))
    suspend fun createOrUpdateFromAuth0(auth0UserInfo: Auth0UserInfo): User?
    fun generateAuthToken(user: User): String
    fun validateToken(token: String): Boolean
    suspend fun getUserFromToken(token: String): User?
    suspend fun findAllByRole(role: String): List<User>
    suspend fun updateActiveStatus(userId: String, isActive: Boolean): User?
}
