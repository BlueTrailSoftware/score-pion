package org.example.notifier.application.useCases.submitApplication

import org.springframework.http.codec.multipart.FilePart

data class SubmitApplicationCommand(
    val name: String,
    val email: String,
    val phone: String?,
    val positionId: String,
    val filePart: FilePart?,
    val linkedinUrl: String?,
    val gdprConsent: Boolean,
    val captchaToken: String
)
