package org.example.notifier.domain.event

import java.time.Instant

data class AssessmentStartedEvent(
    val candidateEmail: String,
    val assessmentId: String,
    val organizationId: String? = null,
    val timestamp: Instant = Instant.now()
)
