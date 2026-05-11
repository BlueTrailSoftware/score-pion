package org.example.notifier.application.useCases.inviteCandidate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class InviteCandidateUseCase(
    private val applicantService: ApplicantService,
    private val openPositionService: OpenPositionService,
    private val assessmentPlatformService: AssessmentPlatformService,
    private val invitationService: InvitationService,
    private val userService: UserService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort
) {

    suspend fun execute(command: InviteCandidateCommand) = coroutineScope {
        val normalizedEmail = command.candidateEmail.trim().lowercase()
        val candidateName = command.candidateName.trim()

        if (applicantService.findByEmailAndPositionId(normalizedEmail, command.positionId) != null) {
            throw IllegalArgumentException("This candidate has already applied to this position on their own")
        }

        val positionAssessments = openPositionService.getPositionAssessments(command.positionId)

        if (positionAssessments.isEmpty()) {
            throw IllegalArgumentException("Position ${command.positionId} has no assessments assigned")
        }

        val allAssessments = assessmentPlatformService.getAvailableAssessments()

        val results = positionAssessments.map { positionAssessment ->
            async(Dispatchers.IO) {
                try {
                    val publicUrl = allAssessments.firstOrNull { it.id == positionAssessment.assessmentId }?.publicUrl
                        ?: throw IllegalArgumentException("Assessment ${positionAssessment.assessmentId} not found or has no public URL")
                    assessmentPlatformService.sendCandidateInvitation(normalizedEmail, publicUrl)
                    invitationService.createInvitation(
                        Invitation(
                            candidateEmail = normalizedEmail,
                            candidateName = candidateName,
                            assessmentId = positionAssessment.assessmentId,
                            openPositionId = command.positionId,
                            recruiterId = command.recruiterId,
                            status = "invited",
                            assessmentName = positionAssessment.assessmentName
                        )
                    )
                    true
                } catch (e: Exception) {
                    logger.error(
                        "Failed to invite {} to assessment {}: {}",
                        normalizedEmail,
                        positionAssessment.assessmentId,
                        e.message
                    )
                    false
                }
            }
        }.awaitAll()

        if (results.any { it }) {
            notifyInvitation(normalizedEmail, candidateName, command.positionId, command.recruiterId, positionAssessments.size)
        }
    }

    private suspend fun notifyInvitation(
        candidateEmail: String,
        candidateName: String,
        positionId: String,
        recruiterId: String,
        assessmentsCount: Int
    ) {
        try {
            val position = openPositionService.getPosition(positionId)
            val recruiter = userService.findById(recruiterId)

            if (position != null && recruiter != null) {
                notificationOrchestrator.notifyCandidateInvitation(
                    candidateEmail = candidateEmail,
                    candidateName = candidateName,
                    positionTitle = position.title,
                    recruiterName = recruiter.name,
                    assessmentsCount = assessmentsCount
                )
                notificationOrchestrator.notifyRecruiterCandidateInvitation(
                    recruiterEmail = recruiter.email,
                    recruiterName = recruiter.name,
                    candidateName = candidateName,
                    candidateEmail = candidateEmail,
                    positionTitle = position.title,
                    assessmentsCount = assessmentsCount
                )
            } else {
                logger.warn("Could not send invitation notification: position or recruiter not found")
            }
        } catch (e: Exception) {
            logger.error("Failed to send candidate invitation notification: {}", e.message)
        }
    }
}