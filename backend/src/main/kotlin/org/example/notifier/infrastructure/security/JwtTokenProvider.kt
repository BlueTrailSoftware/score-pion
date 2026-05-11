package org.example.notifier.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

/**
 * Low-level JWT token provider for generating and validating tokens.
 * Used by AuthTokenService and PrivacyTokenService.
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    /**
     * Generate a JWT token with custom claims and expiration.
     */
    fun generateToken(
        subject: String,
        claims: Map<String, Any> = emptyMap(),
        expirationMs: Long
    ): String {
        val now = Instant.now()
        val expiration = now.plusMillis(expirationMs)

        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))

        claims.forEach { (key, value) -> builder.claim(key, value) }

        return builder.signWith(secretKey).compact()
    }

    /**
     * Generate a JWT token with expiration in hours.
     */
    fun generateTokenWithHoursExpiration(
        subject: String,
        claims: Map<String, Any> = emptyMap(),
        expirationHours: Long
    ): String {
        val now = Instant.now()
        val expiration = now.plus(expirationHours, ChronoUnit.HOURS)

        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))

        claims.forEach { (key, value) -> builder.claim(key, value) }

        return builder.signWith(secretKey).compact()
    }

    /**
     * Parse and validate a JWT token, returning the claims.
     * Returns null if token is invalid or expired.
     */
    fun parseToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract subject from token.
     */
    fun getSubject(token: String): String? = parseToken(token)?.subject

    /**
     * Check if token is valid and not expired.
     */
    fun isTokenValid(token: String): Boolean {
        val claims = parseToken(token) ?: return false
        return !claims.expiration.before(Date())
    }

    /**
     * Extract token from Authorization header (Bearer scheme).
     */
    fun extractFromHeader(authHeader: String?): String? {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            return null
        }
        return authHeader.substring(7)
    }
}
