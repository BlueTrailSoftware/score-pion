package org.example.notifier.infrastructure.controller

import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailCommand
import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.getGlobalRecipientEmails.GetGlobalRecipientEmailsUseCase
import org.example.notifier.application.useCases.getGlobalRecipients.GetGlobalRecipientsUseCase
import org.example.notifier.application.useCases.removeGlobalRecipientEmail.RemoveGlobalRecipientEmailCommand
import org.example.notifier.application.useCases.removeGlobalRecipientEmail.RemoveGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.updateGlobalRecipientEmail.UpdateGlobalRecipientEmailCommand
import org.example.notifier.application.useCases.updateGlobalRecipientEmail.UpdateGlobalRecipientEmailUseCase
import org.example.notifier.infrastructure.dto.request.AddEmailRequest
import org.example.notifier.infrastructure.dto.request.UpdateEmailRequest
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.EmailListResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Settings", description = "Manage global notification email recipients (admin only)")
@RestController
@RequestMapping("/settings/global-recipients")
@PreAuthorize("hasRole('ADMIN')")
class GlobalRecipientsController(
    private val getGlobalRecipientsUseCase: GetGlobalRecipientsUseCase,
    private val getGlobalRecipientEmailsUseCase: GetGlobalRecipientEmailsUseCase,
    private val addGlobalRecipientEmailUseCase: AddGlobalRecipientEmailUseCase,
    private val removeGlobalRecipientEmailUseCase: RemoveGlobalRecipientEmailUseCase,
    private val updateGlobalRecipientEmailUseCase: UpdateGlobalRecipientEmailUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    @Operation(summary = "Get global notification recipients")
    @GetMapping
    suspend fun getRecipients(): ResponseEntity<ApiResponse<GlobalRecipientsResult>> {
        return try {
            val result = getGlobalRecipientsUseCase.execute()
            responseFactory.success("Global recipients retrieved successfully", result)
        } catch (e: Exception) {
            logger.error("Error fetching global recipients: ${e.message}", e)
            responseFactory.error("Failed to fetch global recipients: ${e.message}")
        }
    }

    @Operation(summary = "Get all recipient email addresses")
    @GetMapping("/emails")
    suspend fun getAllEmails(): ResponseEntity<ApiResponse<EmailListResponse>> {
        return try {
            val emails = getGlobalRecipientEmailsUseCase.execute()
            responseFactory.success("Emails retrieved successfully", EmailListResponse(emails))
        } catch (e: Exception) {
            logger.error("Error fetching recipient emails: ${e.message}", e)
            responseFactory.error("Failed to fetch recipient emails: ${e.message}")
        }
    }

    @Operation(summary = "Add a recipient email address")
    @PostMapping("/emails")
    suspend fun addEmail(@RequestBody request: AddEmailRequest): ResponseEntity<ApiResponse<GlobalRecipientsResult>> {
        val userId = securityUtils.getCurrentUserId()
        return try {
            val result = addGlobalRecipientEmailUseCase.execute(
                AddGlobalRecipientEmailCommand(email = request.email, updatedBy = userId)
            )
            responseFactory.success("Email added successfully", result)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request when adding email: ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Error adding email to global recipients: ${e.message}", e)
            responseFactory.error("Failed to add email: ${e.message}")
        }
    }

    @Operation(summary = "Remove a recipient email address")
    @DeleteMapping("/emails/{email}")
    suspend fun removeEmail(@PathVariable email: String): ResponseEntity<ApiResponse<GlobalRecipientsResult>> {
        val userId = securityUtils.getCurrentUserId()
        return try {
            val result = removeGlobalRecipientEmailUseCase.execute(
                RemoveGlobalRecipientEmailCommand(email = email, updatedBy = userId)
            )
            responseFactory.success("Email removed successfully", result)
        } catch (e: IllegalArgumentException) {
            logger.error("Email not found when removing: ${e.message}")
            responseFactory.notFound(e.message ?: "Email not found")
        } catch (e: Exception) {
            logger.error("Error removing email from global recipients: ${e.message}", e)
            responseFactory.error("Failed to remove email: ${e.message}")
        }
    }

    @Operation(summary = "Update an existing recipient email address")
    @PutMapping("/emails")
    suspend fun updateEmail(@RequestBody request: UpdateEmailRequest): ResponseEntity<ApiResponse<GlobalRecipientsResult>> {
        val userId = securityUtils.getCurrentUserId()
        return try {
            val result = updateGlobalRecipientEmailUseCase.execute(
                UpdateGlobalRecipientEmailCommand(
                    oldEmail = request.oldEmail,
                    newEmail = request.newEmail,
                    updatedBy = userId
                )
            )
            responseFactory.success("Email updated successfully", result)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request when updating email: ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Error updating email in global recipients: ${e.message}", e)
            responseFactory.error("Failed to update email: ${e.message}")
        }
    }
}
