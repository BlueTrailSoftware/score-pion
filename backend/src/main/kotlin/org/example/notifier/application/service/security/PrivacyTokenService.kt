package org.example.notifier.application.service.security

import org.example.notifier.infrastructure.security.JwtTokenProvider
import org.springframework.stereotype.Service

/**
 * Privacy Token Service for GDPR data privacy operations.
 * Creates short-lived tokens for data privacy requests (delete/download).
 */
@Service
class PrivacyTokenService(
    private val jwtTokenProvider: JwtTokenProvider
) {
    companion object {
        private const val EXPIRATION_HOURS = 24L
        private const val PURPOSE_CLAIM = "purpose"
        private const val PURPOSE_DATA_DELETION = "data_deletion"
        private const val PURPOSE_DATA_DOWNLOAD = "data_download"
    }

    fun generateDeletionToken(email: String): String =
        jwtTokenProvider.generateTokenWithHoursExpiration(
            subject = email,
            claims = mapOf(PURPOSE_CLAIM to PURPOSE_DATA_DELETION),
            expirationHours = EXPIRATION_HOURS
        )

    fun validateDeletionToken(token: String): String? =
        validateTokenWithPurpose(token, PURPOSE_DATA_DELETION)

    fun generateDownloadToken(email: String): String =
        jwtTokenProvider.generateTokenWithHoursExpiration(
            subject = email,
            claims = mapOf(PURPOSE_CLAIM to PURPOSE_DATA_DOWNLOAD),
            expirationHours = EXPIRATION_HOURS
        )

    fun validateDownloadToken(token: String): String? =
        validateTokenWithPurpose(token, PURPOSE_DATA_DOWNLOAD)

    private fun validateTokenWithPurpose(token: String, expectedPurpose: String): String? {
        val claims = jwtTokenProvider.parseToken(token) ?: return null
        if (claims[PURPOSE_CLAIM] != expectedPurpose) return null
        return claims.subject
    }
}
