package org.example.notifier.application.useCases.updateGlobalRecipientEmail

data class UpdateGlobalRecipientEmailCommand(
    val oldEmail: String,
    val newEmail: String,
    val updatedBy: String
)