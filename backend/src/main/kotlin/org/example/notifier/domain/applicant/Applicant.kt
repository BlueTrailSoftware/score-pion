package org.example.notifier.domain.applicant

import java.time.LocalDateTime
import java.util.UUID

enum class ApplicantStatus {
    PENDING,
    INVITED,
    REJECTED,
    ANONYMIZED
}

data class Applicant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String?,
    val positionId: String,
    val status: ApplicantStatus = ApplicantStatus.PENDING,
    val source: String = "self_application",
    val fileUrl: String? = null,
    val linkedinUrl: String? = null,
    val statusNote: String? = null,

    val gdprConsent: Boolean = false,
    val gdprConsentDate: LocalDateTime? = null,
    val deleteAfter: LocalDateTime,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var reviewedBy: String? = null,
    var reviewedAt: LocalDateTime? = null,
    val isFileDeleted: Boolean = false
)