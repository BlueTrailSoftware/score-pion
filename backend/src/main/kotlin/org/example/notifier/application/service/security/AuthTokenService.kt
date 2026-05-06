package org.example.notifier.application.service.security

import org.example.notifier.infrastructure.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Authentication Token Service for user login and session management.
 * Handles JWT tokens for authenticated users with roles and permissions.
 */
@Service
class AuthTokenService(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${jwt.expiration:86400000}") private val expiration: Long // 24 hours default
) {

    /**
     * Generate JWT token for user authentication.
     */
    fun generateToken(userId: String, email: String, role: String): String {
        return jwtTokenProvider.generateToken(
            subject = userId,
            claims = mapOf("email" to email, "role" to role),
            expirationMs = expiration
        )
    }

    /**
     * Extract user ID from token.
     */
    fun getUserIdFromToken(token: String): String? = jwtTokenProvider.getSubject(token)

    /**
     * Validate JWT token.
     */
    fun validateToken(token: String): Boolean = jwtTokenProvider.isTokenValid(token)

    /**
     * Extract token from Authorization header.
     */
    fun extractTokenFromHeader(authHeader: String?): String? = jwtTokenProvider.extractFromHeader(authHeader)

    /** Parses [token] once, validates signature and expiry, and returns its subject (userId), or null if invalid. */
    fun validateAndExtractUserId(token: String): String? = jwtTokenProvider.parseToken(token)?.subject
}
