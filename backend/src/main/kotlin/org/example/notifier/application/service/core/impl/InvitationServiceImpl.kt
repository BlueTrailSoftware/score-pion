package org.example.notifier.application.service.core.impl

import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.port.InvitationRepository
import org.springframework.stereotype.Service
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.infrastructure.logging.LoggerPort
import java.time.LocalDateTime

@Service
class InvitationServiceImpl(
    private val invitationRepository: InvitationRepository,
    private val openPositionService: OpenPositionService,
) : InvitationService {

    /**
     * Find invitation by ID
     */
    override suspend fun findById(id: String): Invitation? {
        return invitationRepository.findById(id)
    }

    /**
     * Search by candidateEmail and assessmentId
     */
    override suspend fun findByCandidateAndAssessment(candidateEmail: String, assessmentId: String): Invitation? {
        return invitationRepository.findByCandidateEmailAndAssessmentId(candidateEmail, assessmentId)
    }

    /**
     * Update invitation
     */
    override suspend fun updateInvitationWithResults(
        candidateEmail: String,
        assessmentId: String,
        report: AssessmentReport
    ): Invitation {
        val invitation = invitationRepository.findByCandidateEmailAndAssessmentId(candidateEmail, assessmentId)
            ?: throw IllegalArgumentException("Invitation not found for candidate: $candidateEmail, assessment: $assessmentId")

        val updatedInvitation = invitation.copy(
            status = report.status.ifBlank { invitation.status },
            finalScore = report.finalScore,
            qualified = report.isQualified,
            completedAt = LocalDateTime.now(),
            assessmentName = report.displayName,
            mcScore = report.mcScore,
            codeScore = report.codeScore,
            plagiarism = report.cheatingDetails?.plagiarism,
            pastedCode = report.cheatingDetails?.pastedCode,
            suspiciousActivity = report.cheatingDetails?.suspiciousActivity,
            aiUsage = report.cheatingDetails?.aiUsage,
            tabSwitchCount = report.cheatingDetails?.tabLeaving
        )

        return invitationRepository.save(updatedInvitation)
    }

    override suspend fun updateInvitationStatus(
        candidateEmail: String,
        assessmentId: String,
        status: String
    ): Invitation {
        val invitation = invitationRepository.findByCandidateEmailAndAssessmentId(candidateEmail, assessmentId)
            ?: throw IllegalArgumentException("Invitation not found for candidate: $candidateEmail, assessment: $assessmentId")

        return invitationRepository.save(invitation.copy(status = status))
    }

    /**
     * Create invitation
     */
    override suspend fun createInvitation(newInvitation: Invitation): Invitation {
        return invitationRepository.save(newInvitation)
    }

    /**
     * Find all invitations
     */
    override suspend fun findAll(): List<Invitation> {
        return invitationRepository.findAll()
    }

    /**
     * Find all invitations sent by a recruiter
     */
    override suspend fun findByRecruiterId(recruiterId: String): List<Invitation> {
        return invitationRepository.findByRecruiterId(recruiterId)
    }

    /**
     * Returns true if any invitation exists for the given candidate email and position
     */
    override suspend fun existsForCandidateAndPosition(candidateEmail: String, positionId: String): Boolean {
        val assessments = openPositionService.getPositionAssessments(positionId)
        return assessments.any { assessment ->
            invitationRepository.findByCandidateEmailAndAssessmentId(candidateEmail, assessment.assessmentId) != null
        }
    }
}
