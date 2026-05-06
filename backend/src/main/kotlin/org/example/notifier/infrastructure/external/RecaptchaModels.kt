package org.example.notifier.infrastructure.external

/**
 * Response from Google reCAPTCHA verification API.
 */
data class RecaptchaResponse(
    val success: Boolean,
    val score: Double = 0.0,
    val action: String = "",
    val challenge_ts: String = "",
    val hostname: String = "",
    val errorCodes: List<String>? = null
)
