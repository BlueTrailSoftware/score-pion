package org.example.notifier.domain.event

data class AssessmentStartedNotificationEvent(
    val candidateEmail: String,
    val assessmentId: String
)
