package org.example.notifier.application.service.core

import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.invitation.Invitation

interface InvitationService {
    suspend fun findById(id: String): Invitation?
    suspend fun findByCandidateAndAssessment(candidateEmail: String, assessmentId: String): Invitation?
    suspend fun updateInvitationWithResults(
        candidateEmail: String,
        assessmentId: String,
        report: AssessmentReport
    ): Invitation
    suspend fun updateInvitationStatus(
        candidateEmail: String,
        assessmentId: String,
        status: String
    ): Invitation
    suspend fun createInvitation(newInvitation: Invitation): Invitation
    suspend fun findAll(): List<Invitation>
    suspend fun findByRecruiterId(recruiterId: String): List<Invitation>
    suspend fun existsForCandidateAndPosition(candidateEmail: String, positionId: String): Boolean
}
