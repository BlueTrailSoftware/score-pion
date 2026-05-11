package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketResult
import org.example.notifier.infrastructure.dto.response.CreateTicketResponse

internal fun CreateAsanaTicketResult.toResponse() =
    CreateTicketResponse(
        success = success,
        taskId = taskId,
        taskUrl = taskUrl,
        message = message
    )
