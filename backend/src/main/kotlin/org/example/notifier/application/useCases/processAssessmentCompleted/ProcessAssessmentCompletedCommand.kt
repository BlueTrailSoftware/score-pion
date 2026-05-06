package org.example.notifier.application.useCases.processAssessmentCompleted

data class ProcessAssessmentCompletedCommand(
    val candidateEmail: String,
    val assessmentId: String,
    val isReportReady: Boolean,
    val wasTimeExpired: Boolean,
    val organizationId: String? = null
)
