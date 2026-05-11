package org.example.notifier.application.useCases.removeGlobalRecipientEmail

data class RemoveGlobalRecipientEmailCommand(
    val email: String,
    val updatedBy: String
)