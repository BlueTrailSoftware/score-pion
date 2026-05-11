package org.example.notifier.infrastructure.dto.request

/**
 * Request for GDPR privacy operations (deletion or download).
 */
data class PrivacyRequest(
    val email: String,
    val captchaToken: String
)

/**
 * Request to confirm a privacy operation with verification token.
 */
data class PrivacyConfirmRequest(
    val token: String
)
