package org.example.notifier.application.useCases.updateApplicantStatus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.applicant.toApplicantItem
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.shared.SystemConstants
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class UpdateApplicantStatusUseCase(
    private val applicantService: ApplicantService,
    private val openPositionService: OpenPositionService,
    private val assessmentPlatformService: AssessmentPlatformService,
    private val invitationService: InvitationService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort
) {

    suspend fun execute(command: UpdateApplicantStatusCommand): ApplicantItem {
        val savedApplicant = applicantService.updateApplicantStatus(
            id = command.id,
            newStatus = command.newStatus,
            reviewedBy = command.reviewedBy,
            statusNote = command.statusNote
        )

        val position = openPositionService.getPosition(savedApplicant.positionId)

        dispatchStatusNotification(savedApplicant, position, command.reviewedBy.email, command.statusNote)

        return savedApplicant.toApplicantItem(position?.title)
    }

    private suspend fun dispatchStatusNotification(
        applicant: Applicant,
        position: OpenPosition?,
        reviewedByEmail: String,
        statusNote: String?
    ) {
        when (applicant.status) {
            ApplicantStatus.INVITED -> {
                if (position != null) {
                    try {
                        notificationOrchestrator.notifyApplicantApproval(
                            applicantEmail = applicant.email,
                            applicantName = applicant.name,
                            positionTitle = position.title,
                            reviewedBy = reviewedByEmail
                        )
                    } catch (e: Exception) {
                        logger.error("Failed to send approval notification for applicant {}: {}", applicant.id, e.message)
                    }
                } else {
                    logger.warn("Position not found for applicant {}, skipping notification", applicant.id)
                }
                sendAssessmentInvitations(applicant.email, applicant.name, applicant.positionId)
            }
            ApplicantStatus.REJECTED -> {
                if (position != null) {
                    try {
                        notificationOrchestrator.notifyApplicantRejection(
                            applicantEmail = applicant.email,
                            applicantName = applicant.name,
                            positionTitle = position.title,
                            statusNote = statusNote,
                            reviewedBy = reviewedByEmail
                        )
                    } catch (e: Exception) {
                        logger.error("Failed to send rejection notification for applicant {}: {}", applicant.id, e.message)
                    }
                } else {
                    logger.warn("Position not found for applicant {}, skipping notification", applicant.id)
                }
            }
            else -> logger.debug("No notification sent for status: {}", applicant.status)
        }
    }

    private suspend fun sendAssessmentInvitations(
        candidateEmail: String,
        candidateName: String,
        positionId: String
    ) = coroutineScope {
        val positionAssessments = openPositionService.getPositionAssessments(positionId)

        if (positionAssessments.isEmpty()) {
            throw IllegalArgumentException("Position $positionId has no assessments assigned")
        }

        val allAssessments = assessmentPlatformService.getAvailableAssessments()

        positionAssessments.map { positionAssessment ->
            async(Dispatchers.IO) {
                val publicUrl = allAssessments.firstOrNull { it.id == positionAssessment.assessmentId }?.publicUrl
                if (publicUrl == null) {
                    logger.error(
                        "Assessment {} not found in available list, skipping invitation for {}",
                        positionAssessment.assessmentId,
                        candidateEmail
                    )
                    return@async
                }

                try {
                    assessmentPlatformService.sendCandidateInvitation(candidateEmail, publicUrl)
                } catch (e: Exception) {
                    logger.error(
                        "Failed to send platform invitation to {} for assessment {}: {}",
                        candidateEmail,
                        positionAssessment.assessmentId,
                        e.message
                    )
                    return@async
                }

                try {
                    invitationService.createInvitation(
                        Invitation(
                            candidateEmail = candidateEmail,
                            candidateName = candidateName,
                            assessmentId = positionAssessment.assessmentId,
                            openPositionId = positionId,
                            recruiterId = SystemConstants.SYSTEM_RECRUITER_ID,
                            status = "invited",
                            assessmentName = positionAssessment.assessmentName
                        )
                    )
                } catch (e: Exception) {
                    logger.error(
                        "Platform invitation sent but failed to record in DB for {} assessment {}: {}",
                        candidateEmail,
                        positionAssessment.assessmentId,
                        e.message
                    )
                }
            }
        }.awaitAll()
    }
}
