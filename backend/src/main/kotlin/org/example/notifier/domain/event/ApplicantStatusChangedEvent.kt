package org.example.notifier.domain.event

import org.example.notifier.domain.applicant.ApplicantStatus

data class ApplicantStatusChangedEvent(
    val applicantEmail: String,
    val applicantName: String?,
    val positionTitle: String,
    val newStatus: ApplicantStatus,
    val reviewedBy: String?,
    val statusNote: String? = null
)
