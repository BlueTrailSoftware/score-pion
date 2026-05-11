package org.example.notifier.domain.event

import java.time.Instant

data class AssessmentCompletedEvent(
    val candidateEmail: String,
    val assessmentId: String,
    val isReportReady: Boolean,
    val wasTimeExpired: Boolean,
    val organizationId: String? = null,
    val timestamp: Instant = Instant.now()
)
