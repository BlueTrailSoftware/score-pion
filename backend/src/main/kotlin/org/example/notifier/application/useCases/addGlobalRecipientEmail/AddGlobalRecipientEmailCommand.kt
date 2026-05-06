package org.example.notifier.application.useCases.addGlobalRecipientEmail

data class AddGlobalRecipientEmailCommand(
    val email: String,
    val updatedBy: String
)