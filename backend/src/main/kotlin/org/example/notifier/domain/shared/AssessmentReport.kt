package org.example.notifier.domain.shared

data class AssessmentReport(
    val candidateEmail: String,
    val assessmentId: String,
    val displayName: String,
    val finalScore: Double,
    val mcScore: Int? = null,
    val codeScore: Int? = null,
    val qualifyingScore: Int? = null,
    val status: String,
    val isQualified: Boolean,
    val mcDetails: List<MultipleChoiceDetail>? = null,
    val timeTaken: Int? = null,
    val reportLink: String? = null,
    val workspaces: List<String>? = null,
    val cheatingDetails: CheatingInfo? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class CheatingInfo(
    val tabLeaving: Int,
    val plagiarism: String,
    val pastedCode: String,
    val suspiciousActivity: Boolean,
    val aiUsage: Boolean
)

data class MultipleChoiceDetail(
    val id: String?,
    val question: String?,
    val correct: Boolean?,
    val answer: String?,
    val tags: List<String>? = null
)