package org.example.notifier.application.useCases.processAssessmentJoined

data class ProcessAssessmentJoinedCommand(
    val candidateEmail: String,
    val assessmentId: String,
    val organizationId: String? = null
)
