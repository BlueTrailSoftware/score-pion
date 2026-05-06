package org.example.notifier.application.useCases.processAssessmentCompleted

import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class ProcessAssessmentCompletedUseCase(
    private val assessmentPlatformService: AssessmentPlatformService,
    private val invitationService: InvitationService,
    private val openPositionService: OpenPositionService,
    private val userService: UserService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort
) {

    suspend fun execute(command: ProcessAssessmentCompletedCommand) {
        logger.info(
            "Processing assessment completed — candidate: {}, assessment: {}, reportReady: {}",
            command.candidateEmail,
            command.assessmentId,
            command.isReportReady
        )

        val invitation = findInvitation(command.candidateEmail, command.assessmentId)

        if (command.isReportReady) {
            processWithReport(command, invitation)
        } else {
            processWithoutReport(command, invitation)
        }
    }

    private suspend fun findInvitation(candidateEmail: String, assessmentId: String): Invitation? {
        return try {
            val invitation = invitationService.findByCandidateAndAssessment(candidateEmail, assessmentId)
            if (invitation == null) {
                logger.warn(
                    "No invitation found for candidate: '{}', assessment: '{}'",
                    candidateEmail,
                    assessmentId
                )
            }
            invitation
        } catch (e: Exception) {
            logger.warn(
                "Error looking up invitation for candidate: '{}', assessment: '{}'",
                candidateEmail,
                assessmentId,
                e
            )
            null
        }
    }

    private suspend fun processWithReport(command: ProcessAssessmentCompletedCommand, invitation: Invitation?) {
        val report = fetchCandidateReport(command.candidateEmail, command.assessmentId)

        if (report != null) {
            logger.info(
                "Report retrieved for candidate: {}, timeExpired: {}",
                command.candidateEmail,
                command.wasTimeExpired
            )

            if (invitation != null) {
                updateInvitationWithResults(command.candidateEmail, command.assessmentId, report)
            } else {
                logger.warn(
                    "Invitation not found for candidate: {}, assessment: {} — status will NOT be updated",
                    command.candidateEmail,
                    command.assessmentId
                )
            }

            sendNotificationWithReport(command, report, invitation)
        } else {
            logger.warn(
                "Report not found despite reportReady=true for candidate: {}, assessment: {}",
                command.candidateEmail,
                command.assessmentId
            )
            processWithoutReport(command, invitation)
        }
    }

    private suspend fun processWithoutReport(command: ProcessAssessmentCompletedCommand, invitation: Invitation?) {
        logger.info(
            "Processing completed assessment without report for candidate: {}",
            command.candidateEmail
        )

        val positionTitle = resolvePositionTitle(invitation)
        val recruiter = resolveRecruiter(invitation?.recruiterId)

        try {
            notificationOrchestrator.notifyAssessmentCompletedWithoutReport(
                candidateEmail = command.candidateEmail,
                candidateName = invitation?.candidateName,
                assessmentId = command.assessmentId,
                recruiterEmail = recruiter?.email,
                recruiterName = recruiter?.name,
                positionTitle = positionTitle,
                timeExpired = command.wasTimeExpired
            )
        } catch (e: Exception) {
            logger.error(
                "Error sending no-report notification for candidate: {}, assessment: {}",
                command.candidateEmail,
                command.assessmentId,
                e
            )
        }
    }

    private suspend fun fetchCandidateReport(candidateEmail: String, assessmentId: String): AssessmentReport? {
        return try {
            assessmentPlatformService.getCandidateReport(candidateEmail, assessmentId)
        } catch (e: Exception) {
            logger.error(
                "Error fetching report for candidate: {}, assessment: {}",
                candidateEmail,
                assessmentId,
                e
            )
            null
        }
    }

    private suspend fun updateInvitationWithResults(
        candidateEmail: String,
        assessmentId: String,
        report: AssessmentReport
    ) {
        try {
            invitationService.updateInvitationWithResults(
                candidateEmail = candidateEmail,
                assessmentId = assessmentId,
                report = report
            )
        } catch (e: Exception) {
            logger.error(
                "Error updating invitation for candidate: {}, assessment: {}",
                candidateEmail,
                assessmentId,
                e
            )
        }
    }

    private suspend fun sendNotificationWithReport(
        command: ProcessAssessmentCompletedCommand,
        report: AssessmentReport,
        invitation: Invitation?
    ) {
        try {
            val positionTitle = resolvePositionTitle(invitation)
            val recruiter = resolveRecruiter(invitation?.recruiterId)

            notificationOrchestrator.notifyAssessmentCompletedWithReport(
                candidateEmail = command.candidateEmail,
                candidateName = invitation?.candidateName,
                report = report,
                recruiterEmail = recruiter?.email,
                recruiterName = recruiter?.name,
                positionTitle = positionTitle,
                timeExpired = command.wasTimeExpired
            )
        } catch (e: Exception) {
            logger.error(
                "Error sending notification for candidate: {}, assessment: {}",
                command.candidateEmail,
                command.assessmentId,
                e
            )
        }
    }

    private suspend fun resolvePositionTitle(invitation: Invitation?): String? {
        return invitation?.openPositionId?.let { positionId ->
            try {
                openPositionService.getPosition(positionId)?.title
            } catch (e: Exception) {
                logger.warn("Failed to get position title for ID: {}", positionId, e)
                null
            }
        }
    }

    private suspend fun resolveRecruiter(recruiterId: String?): User? {
        return recruiterId?.let {
            try {
                userService.findById(it)
            } catch (e: Exception) {
                logger.warn("Failed to resolve recruiter for ID: {}", it, e)
                null
            }
        }
    }
}
