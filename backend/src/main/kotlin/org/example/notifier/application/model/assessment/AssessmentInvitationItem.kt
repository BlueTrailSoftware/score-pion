package org.example.notifier.application.model.assessment

import java.time.LocalDateTime

data class AssessmentInvitationItem(
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