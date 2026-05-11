package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketCommand
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketResult
import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketUseCase
import org.example.notifier.infrastructure.dto.request.CreateTicketRequest
import org.example.notifier.infrastructure.dto.mapper.*
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.CreateTicketResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Admin - Tickets", description = "Create Asana tickets from the platform (admin only)")
@RestController
@RequestMapping("/admin/ticket")
@PreAuthorize("hasRole('ADMIN')")
class AdminTicketsController(
    private val createAsanaTicketUseCase: CreateAsanaTicketUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    @Operation(summary = "Create an Asana ticket")
    @PostMapping
    suspend fun createTicket(
        @RequestBody request: CreateTicketRequest
    ): ResponseEntity<ApiResponse<CreateTicketResponse>> {
        return try {
            val userEmail = securityUtils.getCurrentUserEmail()
            val result = createAsanaTicketUseCase.execute(
                CreateAsanaTicketCommand(
                    readyDate = request.readyDate,
                    description = request.description,
                    createdByEmail = userEmail
                )
            )
            responseFactory.success("Ticket created successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Unexpected error when createTask ${e.message}")
            responseFactory.error("Failed to create ticket: ${e.message}")
        }
    }
}


