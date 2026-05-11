package org.example.notifier.application.model.applicant

import java.time.LocalDateTime
import org.example.notifier.application.model.assessment.AssessmentInvitationItem

data class ApplicantItem(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val positionId: String,
    val positionTitle: String?,
    val status: String,
    val source: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val reviewedBy: String?,
    val reviewedAt: LocalDateTime?,
    val fileUrl: String?,
    val linkedinUrl: String?,
    val isFileDeleted: Boolean,
    val statusNote: String?,
    val assessments: List<AssessmentInvitationItem>?
)