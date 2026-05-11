package org.example.notifier.application.useCases.updateApplicantStatus

import org.example.notifier.domain.user.User

data class UpdateApplicantStatusCommand(
    val id: String,
    val newStatus: String,
    val reviewedBy: User,
    val statusNote: String?
)