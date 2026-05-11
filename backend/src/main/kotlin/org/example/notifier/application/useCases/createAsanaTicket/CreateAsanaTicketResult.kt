package org.example.notifier.application.useCases.createAsanaTicket

data class CreateAsanaTicketResult(
    val success: Boolean,
    val taskId: String?,
    val taskUrl: String?,
    val message: String
)
