package org.example.notifier.application.useCases.updateRecruiterActiveStatus

data class UpdateRecruiterActiveStatusCommand(
    val recruiterId: String,
    val isActive: Boolean
)
