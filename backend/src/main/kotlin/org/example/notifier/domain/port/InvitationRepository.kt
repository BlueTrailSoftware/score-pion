package org.example.notifier.domain.port

import org.example.notifier.domain.invitation.Invitation

interface InvitationRepository {
    suspend fun save(invitation: Invitation): Invitation
    suspend fun findById(id: String): Invitation?
    suspend fun findByCandidateEmailAndAssessmentId(candidateEmail: String, assessmentId: String): Invitation?
    suspend fun findAll(): List<Invitation>
    suspend fun findByRecruiterId(recruiterId: String): List<Invitation>
    suspend fun delete(invitation: Invitation)
}
