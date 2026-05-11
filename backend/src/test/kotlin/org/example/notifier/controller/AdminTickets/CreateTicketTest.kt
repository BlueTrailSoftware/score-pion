package org.example.notifier.controller.AdminTickets

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketCommand
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketResult
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.AdminTicketsController
import org.example.notifier.infrastructure.dto.request.CreateTicketRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class AdminTicketsControllerCreateTicketTest {

    private lateinit var createAsanaTicketUseCase: CreateAsanaTicketUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: AdminTicketsController

    private val responseFactory = ResponseEntityFactory()
    private val now = LocalDateTime.now()

    private val currentAdmin = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin Name",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val ticketResult = CreateAsanaTicketResult(
        success = true,
        taskId = "task-123",
        taskUrl = "https://app.asana.com/task-123",
        message = "Task created"
    )

    @BeforeEach
    fun setup() {
        createAsanaTicketUseCase = Mockito.mock(CreateAsanaTicketUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)

        controller = AdminTicketsController(
            createAsanaTicketUseCase = createAsanaTicketUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
        )
    }

    @Test
    fun `createTicket should return 200 with success status`() = runBlocking {
        whenever(securityUtils.getCurrentUserEmail()).thenReturn(currentAdmin.email)
        whenever(createAsanaTicketUseCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-04-01",
                description = "Fix bug",
                createdByEmail = currentAdmin.email
            )
        )).thenReturn(ticketResult)

        val response = controller.createTicket(
            CreateTicketRequest(readyDate = "2026-04-01", description = "Fix bug")
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Ticket created successfully", response.body?.message)
    }

    @Test
    fun `createTicket should map result fields to response`() = runBlocking {
        whenever(securityUtils.getCurrentUserEmail()).thenReturn(currentAdmin.email)
        whenever(createAsanaTicketUseCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-04-01",
                description = "Fix bug",
                createdByEmail = currentAdmin.email
            )
        )).thenReturn(ticketResult)

        val response = controller.createTicket(
            CreateTicketRequest(readyDate = "2026-04-01", description = "Fix bug")
        )

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(true, data!!.success)
        Assertions.assertEquals("task-123", data.taskId)
        Assertions.assertEquals("https://app.asana.com/task-123", data.taskUrl)
    }

    @Test
    fun `createTicket should pass correct command to use case`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUserEmail()).thenReturn(currentAdmin.email)
        whenever(createAsanaTicketUseCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-04-01",
                description = "Fix bug",
                createdByEmail = currentAdmin.email
            )
        )).thenReturn(ticketResult)

        controller.createTicket(
            CreateTicketRequest(readyDate = "2026-04-01", description = "Fix bug")
        )

        verify(createAsanaTicketUseCase).execute(argThat {
            readyDate == "2026-04-01" &&
            description == "Fix bug" &&
            createdByEmail == currentAdmin.email
        })
    }

    @Test
    fun `createTicket should return 400 on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUserEmail()).thenReturn(currentAdmin.email)
        whenever(createAsanaTicketUseCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "invalid-date",
                description = "Fix bug",
                createdByEmail = currentAdmin.email
            )
        )).thenThrow(IllegalArgumentException("Invalid date format"))

        val response = controller.createTicket(
            CreateTicketRequest(readyDate = "invalid-date", description = "Fix bug")
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Invalid date format", response.body?.message)
    }

    @Test
    fun `createTicket should return 400 with default message when exception has no message`() = runBlocking {
        whenever(securityUtils.getCurrentUserEmail()).thenReturn(currentAdmin.email)
        whenever(createAsanaTicketUseCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-04-01",
                description = "Fix bug",
                createdByEmail = currentAdmin.email
            )
        )).thenThrow(IllegalArgumentException())

        val response = controller.createTicket(
            CreateTicketRequest(readyDate = "2026-04-01", description = "Fix bug")
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Invalid request", response.body?.message)
    }

    @Test
    fun `createTicket should return 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUserEmail()).thenReturn(currentAdmin.email)
        whenever(createAsanaTicketUseCase.execute(
            CreateAsanaTicketCommand(
                readyDate = "2026-04-01",
                description = "Fix bug",
                createdByEmail = currentAdmin.email
            )
        )).thenThrow(RuntimeException("Asana API unavailable"))

        val response = controller.createTicket(
            CreateTicketRequest(readyDate = "2026-04-01", description = "Fix bug")
        )

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}