package org.example.notifier.infrastructure.dto.request

data class UpdateApplicantStatusRequest(
    val status: String, // pending | approved | rejected
    val statusNote: String? = null
)
