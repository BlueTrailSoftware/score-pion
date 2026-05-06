package org.example.notifier.infrastructure.dto.request

data class ApplyToPositionRequest(
    val name: String,
    val email: String,
    val phone: String?,
    val positionId: String,
    val linkedinUrl: String? = null,
    val gdprConsent: Boolean = false,
    val captchaToken: String
)
