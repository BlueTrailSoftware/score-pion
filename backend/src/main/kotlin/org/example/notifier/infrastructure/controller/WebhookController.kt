package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.processAssessmentCompleted.ProcessAssessmentCompletedCommand
import org.example.notifier.application.useCases.processAssessmentCompleted.ProcessAssessmentCompletedUseCase
import org.example.notifier.application.useCases.processAssessmentJoined.ProcessAssessmentJoinedCommand
import org.example.notifier.application.useCases.processAssessmentJoined.ProcessAssessmentJoinedUseCase
import org.example.notifier.infrastructure.adapter.coderbyte.webhook.CoderbyteWebhookAdapter
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.external.coderbyte.CoderbyteWebhook
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Webhooks", description = "Incoming Coderbyte assessment event webhooks (no auth required)")
@SecurityRequirements
@RestController
@RequestMapping("/webhook/coderbyte")
class CoderbyteWebhookController(
    private val processAssessmentJoinedUseCase: ProcessAssessmentJoinedUseCase,
    private val processAssessmentCompletedUseCase: ProcessAssessmentCompletedUseCase,
    private val logger: LoggerPort
) {

    @Operation(summary = "Coderbyte assessment joined event")
    @PostMapping("/assessment/joined")
    suspend fun onAssessmentJoined(@RequestBody payload: CoderbyteWebhook): ResponseEntity<Void> {
        return try {
            val event = CoderbyteWebhookAdapter.toAssessmentStartedEvent(payload)
            processAssessmentJoinedUseCase.execute(
                ProcessAssessmentJoinedCommand(
                    candidateEmail = event.candidateEmail,
                    assessmentId = event.assessmentId,
                    organizationId = event.organizationId
                )
            )
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error processing joined webhook: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @Operation(summary = "Coderbyte assessment completed event")
    @PostMapping("/assessment/completed")
    suspend fun onAssessmentCompleted(@RequestBody payload: CoderbyteWebhook): ResponseEntity<Void> {
        return try {
            val event = CoderbyteWebhookAdapter.toAssessmentCompletedEvent(payload)
            processAssessmentCompletedUseCase.execute(
                ProcessAssessmentCompletedCommand(
                    candidateEmail = event.candidateEmail,
                    assessmentId = event.assessmentId,
                    isReportReady = event.isReportReady,
                    wasTimeExpired = event.wasTimeExpired,
                    organizationId = event.organizationId
                )
            )
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error processing completed webhook: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @Operation(summary = "Legacy Coderbyte webhook (deprecated, kept for backward compatibility)")
    @PostMapping
    suspend fun onCoderbyteEvent(@RequestBody payload: CoderbyteWebhook): ResponseEntity<Void> {
        return try {
            val event = CoderbyteWebhookAdapter.toAssessmentCompletedEvent(payload)
            processAssessmentCompletedUseCase.execute(
                ProcessAssessmentCompletedCommand(
                    candidateEmail = event.candidateEmail,
                    assessmentId = event.assessmentId,
                    isReportReady = event.isReportReady,
                    wasTimeExpired = event.wasTimeExpired,
                    organizationId = event.organizationId
                )
            )
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error processing legacy webhook: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
