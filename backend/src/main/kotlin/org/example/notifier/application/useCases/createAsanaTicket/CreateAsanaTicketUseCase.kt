package org.example.notifier.application.useCases.createAsanaTicket

import org.example.notifier.application.service.integration.AsanaService
import org.example.notifier.infrastructure.dto.request.CreateTicketRequest
import org.springframework.stereotype.Component

@Component
class CreateAsanaTicketUseCase(
    private val asanaService: AsanaService
) {

    suspend fun execute(command: CreateAsanaTicketCommand): CreateAsanaTicketResult {
        val response = asanaService.createTask(
            request = CreateTicketRequest(
                readyDate = command.readyDate,
                description = command.description
            ),
            userEmail = command.createdByEmail
        )

        return CreateAsanaTicketResult(
            success = response.success,
            taskId = response.taskId,
            taskUrl = response.taskUrl,
            message = response.message
        )
    }
}
