package org.example.notifier.application.useCases.createAsanaTicket

data class CreateAsanaTicketCommand(
    val readyDate: String,
    val description: String,
    val createdByEmail: String
)
