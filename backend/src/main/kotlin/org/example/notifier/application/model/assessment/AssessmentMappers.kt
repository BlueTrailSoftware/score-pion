package org.example.notifier.application.model.assessment

import org.example.notifier.domain.invitation.Invitation

internal fun Invitation.toAssessmentInvitationItem() = AssessmentInvitationItem(
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