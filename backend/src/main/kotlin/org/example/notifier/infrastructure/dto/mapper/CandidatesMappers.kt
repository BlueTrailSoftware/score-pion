package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.model.assessment.AssessmentInvitationItem
import org.example.notifier.application.model.applicant.CandidateItem
import org.example.notifier.infrastructure.dto.response.AssessmentInvitationDetail
import org.example.notifier.infrastructure.dto.response.CandidateInvitationResponse

internal fun CandidateItem.toResponse() = CandidateInvitationResponse(
    candidateEmail = candidateEmail,
    candidateName = candidateName,
    positionId = positionId,
    positionTitle = positionTitle,
    recruiterId = recruiterId,
    assessments = assessments.map { it.toDetail() },
    invitedAt = invitedAt
)

internal fun AssessmentInvitationItem.toDetail() = AssessmentInvitationDetail(
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