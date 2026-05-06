package org.example.notifier.infrastructure.dto.response

/**
 * Response DTO for ticket creation operations
 */
data class CreateTicketResponse(
    val success: Boolean,
    val taskId: String?,
    val taskUrl: String?,
    val message: String
)
