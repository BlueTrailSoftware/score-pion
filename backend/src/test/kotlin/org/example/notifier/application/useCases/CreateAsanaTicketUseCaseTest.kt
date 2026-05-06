package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.integration.AsanaService
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketCommand
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketUseCase
import org.example.notifier.infrastructure.dto.request.CreateTicketRequest
import org.example.notifier.infrastructure.dto.response.CreateTicketResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class CreateAsanaTicketUseCaseTest {

    private lateinit var asanaService: AsanaService
    private lateinit var useCase: CreateAsanaTicketUseCase

    @BeforeEach
    fun setup() {
        asanaService = mock(AsanaService::class.java)
        useCase = CreateAsanaTicketUseCase(asanaService)
    }

    @Test
    fun `execute should return result mapped from asana service response`() = runBlocking<Unit> {
        val response = CreateTicketResponse(
            success = true,
            taskId = "task-123",
            taskUrl = "https://app.asana.com/task-123",
            message = "Task created successfully in Asana"
        )
        whenever(
            asanaService.createTask(
                request = CreateTicketRequest(readyDate = "2026-04-01", description = "Fix bug"),
                userEmail = "admin@example.com"
            )
        ).thenReturn(response)

        val result = useCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-04-01",
                description = "Fix bug",
                createdByEmail = "admin@example.com"
            )
        )

        assertEquals(true, result.success)
        assertEquals("task-123", result.taskId)
        assertEquals("https://app.asana.com/task-123", result.taskUrl)
        assertEquals("Task created successfully in Asana", result.message)
    }

    @Test
    fun `execute should forward createdByEmail as userEmail to asana service`() = runBlocking<Unit> {
        val response = CreateTicketResponse(success = true, taskId = "t-1", taskUrl = null, message = "ok")
        whenever(
            asanaService.createTask(
                request = CreateTicketRequest(readyDate = "2026-05-01", description = "New task"),
                userEmail = "recruiter@example.com"
            )
        ).thenReturn(response)

        val result = useCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-05-01",
                description = "New task",
                createdByEmail = "recruiter@example.com"
            )
        )

        assertEquals(true, result.success)
    }

    @Test
    fun `execute should propagate exception from asana service`() = runBlocking<Unit> {
        whenever(
            asanaService.createTask(
                request = CreateTicketRequest(readyDate = "2026-04-01", description = "desc"),
                userEmail = "admin@example.com"
            )
        ).thenThrow(RuntimeException("Asana API unavailable"))

        org.junit.jupiter.api.assertThrows<RuntimeException> {
            runBlocking {
                useCase.execute(
                    CreateAsanaTicketCommand(
                        readyDate = "2026-04-01",
                        description = "desc",
                        createdByEmail = "admin@example.com"
                    )
                )
            }
        }
    }
}
