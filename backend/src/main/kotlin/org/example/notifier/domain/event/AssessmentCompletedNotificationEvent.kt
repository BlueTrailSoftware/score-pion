package org.example.notifier.domain.event

import org.example.notifier.domain.shared.AssessmentReport

data class AssessmentCompletedNotificationEvent(
    val candidateEmail: String,
    val candidateName: String?,
    val report: AssessmentReport?,
    val isReportReady: Boolean,
    val assessmentId: String,
    val recruiterEmail: String?,
    val recruiterName: String?,
    val positionTitle: String?,
    val timeExpired: Boolean
)
