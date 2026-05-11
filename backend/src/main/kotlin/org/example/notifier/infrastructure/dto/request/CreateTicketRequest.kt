package org.example.notifier.infrastructure.dto.request

/**
 * Request DTO for creating tickets in our internal API
 */
data class CreateTicketRequest(
    val readyDate: String,
    val description: String
)
