package org.example.notifier.infrastructure.dto.response

import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import java.time.LocalDateTime

data class CandidateInvitationResponse(
    val candidateEmail: String,
    val candidateName: String,
    val positionId: String,
    val positionTitle: String?,
    val recruiterId: String,
    val assessments: List<AssessmentInvitationDetail>,
    val invitedAt: LocalDateTime
)

data class AssessmentInvitationDetail(
    val assessmentId: String,
    val assessmentName: String?,
    val status: String,
    val finalScore: Double?,
    val mcScore: Int?,
    val codeScore: Int?,
    val qualified: Boolean?,
    val completedAt: LocalDateTime?,
    val plagiarism: String?,
    val pastedCode: String?,
    val suspiciousActivity: Boolean?,
    val aiUsage: Boolean?,
    val tabSwitchCount: Int?
)

fun Invitation.toAssessmentDetail() = AssessmentInvitationDetail(
    assessmentId = assessmentId,
    assessmentName = assessmentName,
    status = status,
    finalScore = finalScore,
    mcScore = mcScore,
    codeScore = codeScore,
    qualified = qualified,
    completedAt = completedAt,
    plagiarism = plagiarism,
    pastedCode = pastedCode,
    suspiciousActivity = suspiciousActivity,
    aiUsage = aiUsage,
    tabSwitchCount = tabSwitchCount
)

data class RecruiterInvitationResponse(
    val id: String,
    val email: String,
    val invitedBy: String,
    val assignedPositions: List<String>,
    val status: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val acceptedAt: LocalDateTime?
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val pictureUrl: String? = null
)

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)

// Extension functions for domain models
fun RecruiterInvitation.toResponse(): RecruiterInvitationResponse {
    return RecruiterInvitationResponse(
        id = this.id,
        email = this.email,
        invitedBy = this.invitedBy,
        assignedPositions = this.assignedPositions,
        status = this.status.toString(),
        createdAt = this.createdAt,
        expiresAt = this.expiresAt,
        acceptedAt = this.acceptedAt
    )
}

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        email = this.email,
        name = this.name,
        role = this.role,
        isActive = this.isActive,
        createdAt = this.createdAt
    )
}

// Position DTOs
data class PositionAssessmentResponse(
    val assessmentId: String,
    val assessmentName: String,
    val addedAt: LocalDateTime
)

data class PositionResponse(
    val id: String,
    val title: String,
    val description: String,
    val external: Boolean,
    val assessments: List<PositionAssessmentResponse>,
    val fileUrl: String?,
    val createdBy: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isFileDeleted: Boolean = false,
    val workMode: String,
    val location: String,
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String> = emptyList()
)

data class PositionSummaryResponse(
    val id: String,
    val title: String,
    val description: String,
    val external: Boolean,
    val assessmentsCount: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val workMode: String,
    val location: String
)

data class RecruiterPositionResponse(
    val id: String,
    val title: String,
    val description: String,
    val external: Boolean,
    val assessmentsCount: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val workMode: String,
    val location: String
)

data class GrantPositionAccessResponse(
    val granted: List<String>,
    val alreadyGranted: List<String>
)

data class RecruiterListResponse(
    val id: String,
    val email: String,
    val name: String,
    val isActive: Boolean,
    val status: String?,
    val positionsCount: Int,
    val createdAt: LocalDateTime
)

data class RecruiterDetailResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val positions: List<RecruiterPositionResponse>,
    val positionsCount: Int,
    val createdAt: LocalDateTime
)
