package org.example.notifier.infrastructure.dto.request

data class UpdateApplicantRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val deleteFile: Boolean = false
)
