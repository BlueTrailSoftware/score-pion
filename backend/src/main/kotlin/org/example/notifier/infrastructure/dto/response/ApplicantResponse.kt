package org.example.notifier.infrastructure.dto.response

import org.example.notifier.domain.applicant.Applicant
import java.time.LocalDateTime

data class ApplicantResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val positionId: String,
    val positionTitle: String? = null,
    val status: String,
    val source: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val reviewedBy: String? = null,
    val reviewedAt: LocalDateTime? = null,
    val fileUrl: String? = null,
    val linkedinUrl: String? = null,
    val isFileDeleted: Boolean = false,
    val statusNote: String? = null,
    val assessments: List<AssessmentInvitationDetail>? = null
)

fun Applicant.toResponse(
    positionTitle: String? = null,
    assessments: List<AssessmentInvitationDetail>? = null
) = ApplicantResponse(
    id = id,
    name = name,
    email = email,
    phone = phone.orEmpty(),
    positionId = positionId,
    positionTitle = positionTitle,
    status = status.name,
    source = source,
    createdAt = createdAt,
    updatedAt = updatedAt,
    reviewedBy = reviewedBy,
    reviewedAt = reviewedAt,
    fileUrl = fileUrl,
    linkedinUrl = linkedinUrl,
    isFileDeleted = isFileDeleted,
    statusNote = statusNote,
    assessments = assessments
)

/**
 * Simplified position info for public listing Used by external candidates to browse open positions
 */
data class PublicPositionResponse(
        val id: String,
        val title: String,
        val description: String,
        val fileUrl: String?,
        val createdAt: LocalDateTime,
        val workMode: String,
        val location: String,
        val jobType: String? = null,
        val experienceMin: Int? = null,
        val experienceMax: Int? = null,
        val skills: List<String> = emptyList()
)
