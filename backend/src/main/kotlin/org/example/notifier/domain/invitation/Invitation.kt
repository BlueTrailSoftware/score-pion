package org.example.notifier.domain.invitation

import java.time.LocalDateTime
import java.util.UUID

data class Invitation(
    val id: String = UUID.randomUUID().toString(),
    val candidateEmail: String,
    val candidateName: String,
    val assessmentId: String,
    val openPositionId: String,
    val recruiterId: String,
    val status: String = "pending",
    val finalScore: Double? = null,
    val qualified: Boolean? = null,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val assessmentName: String? = null,
    val mcScore: Int? = null,
    val codeScore: Int? = null,
    val plagiarism: String? = null,
    val pastedCode: String? = null,
    val suspiciousActivity: Boolean? = null,
    val aiUsage: Boolean? = null,
    val tabSwitchCount: Int? = null
)