package org.example.notifier.application.model.applicant

import java.time.LocalDateTime
import org.example.notifier.application.model.assessment.AssessmentInvitationItem

data class CandidateItem(
    val candidateEmail: String,
    val candidateName: String,
    val positionId: String,
    val positionTitle: String?,
    val recruiterId: String,
    val assessments: List<AssessmentInvitationItem>,
    val invitedAt: LocalDateTime
)