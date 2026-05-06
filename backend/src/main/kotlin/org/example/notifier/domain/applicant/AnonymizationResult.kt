package org.example.notifier.domain.applicant

data class AnonymizationResult(
    val applicantId: String,
    val fileDeleted: Boolean,
    val success: Boolean,
    val error: String? = null
)