package org.example.notifier.application.useCases.processAssessmentJoined

import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class ProcessAssessmentJoinedUseCase(
    private val invitationService: InvitationService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort
) {

    suspend fun execute(command: ProcessAssessmentJoinedCommand) {
        logger.info(
            "Processing assessment joined — candidate: {}, assessment: {}",
            command.candidateEmail,
            command.assessmentId
        )

        try {
            invitationService.updateInvitationStatus(
                candidateEmail = command.candidateEmail,
                assessmentId = command.assessmentId,
                status = "in_progress"
            )
        } catch (e: Exception) {
            logger.warn("Could not update invitation status to in_progress: {}", e.message)
        }

        notificationOrchestrator.notifyAssessmentStarted(
            candidateEmail = command.candidateEmail,
            assessmentId = command.assessmentId
        )
    }
}
